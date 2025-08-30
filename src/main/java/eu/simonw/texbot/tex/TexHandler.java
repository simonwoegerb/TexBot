package eu.simonw.texbot.tex;

import eu.simonw.texbot.TexBot;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.FutureUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TexHandler {
    public static final String APP = "app/";
    public static final String TEMPLATE_STANDALONE = APP + "tex/template_standalone.tex";
    public static final String TEMPLATE_FULL = APP +  "tex/template_full.tex";

    public static final String DIRECTORY = APP + "tex/uuid";
    public static final String BODY = "%BODY";
    public enum ConversionType {
        TexToPdf,
        TexToPng
    }
    private final Logger LOGGER = LoggerFactory.getLogger(TexHandler.class);

    public boolean verify(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder().command(command);
        try {
            var p = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String firstline = reader.readLine();
                if (firstline != null) {
                    LOGGER.info("{}: {}", command[0], firstline);
                    return true;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    @Nullable
    public CompletableFuture<Path> convert(String code, ConversionType conversionType) {
        switch (conversionType) {
            case TexToPdf -> {
                return convertTexToPdf(code, TEMPLATE_FULL);
            }
            case TexToPng -> {
                var pdf = convertTexToPdf(code, TEMPLATE_STANDALONE);
                return pdf.thenCompose(this::convertPdfToPng);
            }
        }

        return null;
    }
    private CompletableFuture<Path> convertPdfToPng(Path pdf_file)  {
        String[] command = {
                "pdftoppm",    // pdftoppm executable
                "-png",        // output format as PNG
                "-r", "300",   // resolution of 300 DPI
                "-f", "1",     // convert first page
                "-l", "1",     // convert last page (same as first for single page)
                "main.pdf",    // input PDF file
                "main"         // output prefix (output will be 'main-1.png')
        };
        LOGGER.info("PDF FILE FOR PNG CONV: {}", pdf_file.toAbsolutePath().toString());
        ProcessBuilder pb = new ProcessBuilder().directory(pdf_file.toAbsolutePath().getParent().toFile()).command(command);
        return CompletableFuture.supplyAsync(() -> {
        try {
            Process p = pb.start();

            int rescode = p.waitFor();
            if (rescode != 0) {
                LOGGER.error("ERROR {}", rescode);
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
    @Nullable
    private CompletableFuture<Path> convertTexToPdf(String code, String template) {
    Path main = generateMainFile(code, template);
    if (main == null) {
        LOGGER.error("Main is null");
        return null;
    }
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.info("Entering Future");
                ProcessBuilder pb = new ProcessBuilder().directory(main.getParent().toFile()).command("latexmk", "main.tex");
                try {
                    Process p = pb.start();
                    int rescode = p.waitFor();
                    if (rescode != 0) {
                        LOGGER.error("ERROR {}", rescode);
                    }
                    // Resolving the PDF path relative to the main.tex file
                    return main.getParent().resolve("main.pdf");

                } catch (IOException | InterruptedException e) {
                    LOGGER.error("{}", e.getMessage());
                    return null;
                }
            });

    }
    @SuppressWarnings("unused")
    private Path generateMainFile(String code, String template_type) {
        Path template = Path.of(template_type);
        UUID random_uuid = UUID.randomUUID();
        Path new_directory  = Path.of(DIRECTORY.replaceFirst("uuid", random_uuid.toString()));
        LOGGER.info("Converting a Tex File to Pdf : {}", random_uuid);
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
            Files.write(main_file,new_code, StandardCharsets.UTF_8);
            LOGGER.info("Copied File : {} -> {}", template_type, main_file);
            return main_file;

        } catch (IOException e) {
            return null;
        }
    }


    public boolean setup_file(String name) {
        LOGGER.info("Setting up file: {}", name);
        if (name.startsWith(APP)) name = name.substring(APP.length());
        LOGGER.info("Fixed name: {}",name);


        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                LOGGER.info("Input stream is null");
                return false;
            }
            Path file= Path.of(APP + name);
            Files.createDirectories(file.getParent());
            LOGGER.info("File created. {}", file);
            Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("File is copied. {}", file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
