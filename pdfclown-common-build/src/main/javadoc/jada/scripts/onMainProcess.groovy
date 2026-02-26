/*
  This script tweaks the generated Javadoc.
 */

import static org.pdfclown.common.util.Objects.any
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG
import static org.pdfclown.common.util.io.Files.extension

import java.nio.file.Path
import org.jspecify.annotations.Nullable
import org.pdfclown.jada.core.proc.JadaFileProcess
import org.pdfclown.jada.core.proc.JadaFileProcessor
import org.pdfclown.jada.core.system.SystemConfig
import org.pdfclown.jada.core.system.proc.FileProcess
import org.pdfclown.jada.core.system.proc.TextSerializer


self.callSuper()

/*
 * [shading] Map shaded packages in Javadoc!
 *
 * org.pdfclown.common.util --> org.pdfclown.common.build.shaded.util
 */
self.getConfig().getOperation(JadaFileProcess.class).addProcessor(
        new JadaFileProcessor<String>(new TextSerializer()) {
    @Override
    int getPriority() {
        return 100
    }

    @Override
    void init(SystemConfig config) {
        super.init(config)

        ((TextSerializer) serializer).setCharset(config.getOutputCharset())
    }

    @Override
    boolean isProcessable(Path file, FileProcess.Context context) {
        return any(extension(file), String::equalsIgnoreCase, FILE_EXTENSION__HTML,
            FILE_EXTENSION__SVG)
    }

    @Override
    @Nullable String processContent(String content, Path file, FileProcess.Context context) {
        var newContent = content.replace("org.pdfclown.common.util",
            "org.pdfclown.common.build.shaded.util")
        if(newContent == content)
            return null

        context.changeFile()
        return newContent
    }
})
