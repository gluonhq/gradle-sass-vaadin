package com.gluonhq.gradle

import com.vaadin.sass.internal.ScssStylesheet
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl
import com.vaadin.sass.internal.handler.SCSSErrorHandler

import static com.gluonhq.gradle.JavaFXTools.*

/**
 * @author Radu Andries
 */
class SassCompilerImpl {
    File scssFile
    File scssDir
    File outDir
    ProjectAwareResolver resolver
    Boolean minify = false
    Boolean silent = false
    Boolean javafx = false


    def exec(){
        SCSSErrorHandler handler = silent? new SilentErrorHandler(): new SCSSErrorHandler()

        def sourceScss = scssFile

        ScssStylesheet sass = ScssStylesheet.get(sourceScss.absolutePath, null, new SCSSDocumentHandlerImpl(),handler)
        sass.setFile(sourceScss)

        // setting charset breaks JavaFX CSS generation
        // and should be turned off for JavaFX applications
        if ( !javafx ) {
            sass.setCharset('UTF-8')
        }

        sass.addResolver(resolver.getFSResolver())
        sass.addResolver(resolver)
        sass.compile()

        def cssFileName = scssFile.getAbsolutePath()
                                  .replaceAll('\\.scss','.css')
                                  .replaceFirst( scssDir.getAbsolutePath(), outDir.getAbsolutePath())


        File file = new File(cssFileName)
        file.getParentFile().mkdirs()
        file.withWriter {
            sass.write(it, minify)
        }

        if (javafx) {
            // replace all fakeValues with nulls as it supposed to be
            file.write( file.text.replaceAll(":\\s*$fakeValue\\s*;", ': null;') )
        }
    }


}
