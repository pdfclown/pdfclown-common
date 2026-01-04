/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only
 */
import static org.pdfclown.common.util.Objects.any
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG
import static org.pdfclown.common.util.io.Files.extension

import java.nio.file.Path
import org.jspecify.annotations.Nullable
import org.pdfclown.jada.core.JadaScriptExtension
import org.pdfclown.jada.core.event.MainProcessEvent
import org.pdfclown.jada.core.proc.JadaFileProcess
import org.pdfclown.jada.core.proc.JadaFileProcessor
import org.pdfclown.jada.core.system.SystemConfig
import org.pdfclown.jada.core.system.proc.FileProcess
import org.pdfclown.jada.core.system.proc.TextSerializer

/**
 * Tweaks the generated Javadoc.
 */
class ScriptExtension extends JadaScriptExtension {
    @Override
    void onMainProcess(MainProcessEvent event) {
        /*
         * [shading] Map shaded packages in Javadoc!
         *
         * org.pdfclown.common.util --> org.pdfclown.common.build.util
         */
        getConfig().getOperation(JadaFileProcess.class).addProcessor(
                new JadaFileProcessor<String>(new TextSerializer()) {
            @Override
            public int getPriority() {
                return 100
            }

            @Override
            public void init(SystemConfig config) {
                super.init(config)

                ((TextSerializer) serializer).setCharset(config.getOutputCharset())
            }

            @Override
            public boolean isProcessable(Path file, FileProcess.Context context) {
                return any(extension(file), String::equalsIgnoreCase,
                         FILE_EXTENSION__HTML, FILE_EXTENSION__SVG)
            }

            @Override
            protected @Nullable String processContent(String content, Path file,
                                                      FileProcess.Context context) {
                var newContent = content.replace(
                        "org.pdfclown.common.util",
                        "org.pdfclown.common.build.util")
                if(!newContent.equals(content)) {
                    context.changeFile()
                    return newContent
                }
                return null
            }
        })
    }
}
