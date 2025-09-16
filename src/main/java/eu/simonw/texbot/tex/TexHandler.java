package eu.simonw.texbot.tex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TexHandler {
    public final String APP = "app/";
    public final String TEMPLATE_STANDALONE = APP + "tex/template_standalone.tex";
    public final String TEMPLATE_FULL = APP + "tex/template_full.tex";
    private final String[] fireJailCommand = {"firejail",
            "--caps.drop=all", // drop all root capabilities
            "--net=none", //no network access
            "--private=%DIRECTORY",
            "--whitelist=/usr/bin/latexmk",
            "--whitelist=/usr/bin/pdflatex",
            "--whitelist=/usr/share/texlive",
            "--whitelist=/usr/share/pdftoppm"
    };
    private final int fireJailPrivate = 3;
    private final String[] pdfToPngCommand = new String[]{
        "pdftoppm",    // pdftoppm executable
            "-png",        // output format as PNG
            "-r", "300",   // resolution of 300 DPI
            "-f", "1",     // convert first page
            "-l", "1",     // convert last page (same as first for single page)
            "main.pdf",    // input PDF file
            "main"         // output prefix (output will be 'main-1.png')
    };
    private final String[] texToPdfCommand = {"latexmk", "--no-shell-escape", "main.tex" };

    public final String DIRECTORY = APP + "tex/uuid";
    public final String BODY = "%BODY";

    public enum ConversionType {
        TexToPdf,
        TexToPng
    }

    private final Logger LOGGER = LoggerFactory.getLogger(TexHandler.class);

    @NotNull
    private String[] concatArrays(String[] s1, String[] s2) {
        return Stream.concat(Arrays.stream(s1),Arrays.stream(s2)).toArray(String[]::new);
    }

    @Nullable
    public CompletableFuture<Path> convert(String code, ConversionType conversionType) {
        switch (conversionType) {
            case TexToPdf -> {
                return convertTexToPdf(code, TEMPLATE_FULL);
            }
            case TexToPng -> {
                return convertTexToPdf(code, TEMPLATE_STANDALONE).thenCompose(this::convertPdfToPng);
            }
        }

        return null;
    }
    @NotNull
    private CompletableFuture<Path> convertPdfToPng(Path pdf_file) {

        LOGGER.info("PDF FILE FOR PNG CONV: {}", pdf_file.toAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder().directory(pdf_file.toAbsolutePath().getParent().toFile()).command(pdfToPngCommand);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Process p = pb.start();

                boolean res = p.waitFor(30, TimeUnit.SECONDS);
                if (!res) {
                    p.destroyForcibly();
                    LOGGER.error("ERROR {}", res);
                }
                // Resolving the PDF path relative to the main.tex file
                LOGGER.info("resolving main.png");
                return pdf_file.getParent().resolve("main-1.png");

            } catch (IOException | InterruptedException e) {
                LOGGER.error("{}", e.getMessage());
                return null;
            }
        });


    }
    @NotNull
    private CompletableFuture<Path> convertTexToPdf(String code, String template) {
        CompletableFuture<Path> futuremain = generateMainFile(code, template);

        return futuremain.thenCompose((main) -> {
            LOGGER.info("Entering Future");
            String[] command = concatArrays(fireJailCommand,texToPdfCommand);
            command[fireJailPrivate] = command[fireJailPrivate].replaceFirst("%DIRECTORY", main.getParent().toAbsolutePath().toString());
            ProcessBuilder pb = new ProcessBuilder().directory(main.getParent().toFile()).command(command);
            try {
                Process p = pb.start();
                boolean success = p.waitFor(30, TimeUnit.SECONDS);
                if (!success) {
                    p.destroyForcibly();
                    LOGGER.error("Process for {} was not successful {}", main.toAbsolutePath().toString(),p.exitValue());
                    return CompletableFuture.failedFuture(new RuntimeException("Something went wrong, converting tex to pdf. see Log for " + main.toAbsolutePath().toString()));
                } else
                    // Resolving the PDF path relative to the main.tex file
                    return CompletableFuture.completedFuture(main.getParent().resolve("main.pdf"));

            } catch (IOException | InterruptedException e) {
                LOGGER.error("{}", e.getMessage());
                return CompletableFuture.failedFuture(e);
            }


        });

    }
    @NotNull
    @SuppressWarnings("unused")
    private CompletableFuture<Path> generateMainFile(String code, String template_type) {
        Path template = Path.of(template_type);
        UUID random_uuid = UUID.randomUUID();
        Path new_directory = Path.of(DIRECTORY.replaceFirst("uuid", random_uuid.toString()));
        LOGGER.info("Converting a Tex File to Pdf : {}", random_uuid);
        return CompletableFuture.supplyAsync(() -> {
        try {
            LOGGER.info("Attempting to create directory: {}", new_directory);
            Files.createDirectories(new_directory);
            LOGGER.info("Created directory: {}", new_directory);
            LOGGER.info("Attempting to open file: {}", TEMPLATE_STANDALONE);
            Path main_file = new_directory.resolve(Path.of("main.tex"));
            List<String> lines = Files.readAllLines(template);
            List<String> new_code = lines.stream().map(s -> {
                if (s.equalsIgnoreCase(BODY)) {
                    return code;
                }
                return s;
            }).toList();
            new_code.forEach(LOGGER::info);
            Files.write(main_file, new_code, StandardCharsets.UTF_8);
            LOGGER.info("Copied File : {} -> {}", template_type, main_file);
            return main_file;

        } catch (IOException e) {
            return null;
        }
    });
    }


    public boolean setup_file(String name) {
        LOGGER.info("Setting up file: {}", name);
        if (name.startsWith(APP)) name = name.substring(APP.length());
        LOGGER.info("Fixed name: {}", name);


        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                LOGGER.info("Input stream is null");
                return false;
            }
            Path file = Path.of(APP + name);
            Files.createDirectories(file.getParent());
            LOGGER.info("File created. {}", file);
            Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("File is copied. {}", file);
            return true;
        } catch (IOException e) {
            return false;
        }

    }
    // List of dangerous LaTeX commands or patterns

    // List of dangerous LaTeX commands/patterns (case-insensitive)
    private static final Pattern[] DANGEROUS_PATTERNS = new Pattern[]{
            // File I/O and shell escape
            Pattern.compile("\\\\write18", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\write(?!18)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\input", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\include", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\openout", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\read", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\new(write|read)\\b", Pattern.CASE_INSENSITIVE),

            // Dangerous package loading
            Pattern.compile("\\\\usepackage\\s*\\{\\s*(shellesc|verbatim|catchfile|filecontents|tikz|pgf)\\s*\\}", Pattern.CASE_INSENSITIVE),

            // Command/macro creation or redefinition
            Pattern.compile("\\\\(e)?def\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\(loop|repeat|newcommand|renewcommand)\\b", Pattern.CASE_INSENSITIVE),

            // Execution hooks and expansion tricks
            Pattern.compile("\\\\every[a-zA-Z]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\(expandafter|noexpand)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\special\\b", Pattern.CASE_INSENSITIVE),

            // Obfuscated or indirect execution
            Pattern.compile("\\\\catcode", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\csname.*?\\\\endcsname", Pattern.CASE_INSENSITIVE),

            // Escaped backslashes or obfuscated unicode
            Pattern.compile("[\\\\]{2,}input", Pattern.CASE_INSENSITIVE),     // \\input
            Pattern.compile("\\\\u005cinput", Pattern.CASE_INSENSITIVE),      // Unicode escape for '\input'

            // Commented attempts
            Pattern.compile("%.*?\\\\input", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\\\input%.*?\\n", Pattern.CASE_INSENSITIVE)
    };

    public boolean isSafeLatex(String input) {
        if (input == null || input.isEmpty()) {
            return true; // Empty or null is considered safe
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return false; // Unsafe pattern found
            }
        }

        return true; // No unsafe patterns
    }
    public CompletableFuture<String> isSafeLatexAsync(String latex) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isSafeLatex(latex)) {
                throw new SecurityException("Unsafe LaTeX code detected");
            }
            return latex;
        });
    }



}
