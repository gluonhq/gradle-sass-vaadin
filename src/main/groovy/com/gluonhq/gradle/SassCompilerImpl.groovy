package com.gluonhq.gradle

import com.vaadin.sass.internal.ScssStylesheet
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl
import com.vaadin.sass.internal.handler.SCSSErrorHandler

import static com.gluonhq.gradle.JavaFXTools.*

/**
 * @author Radu Andries
 */
class SassCompilerImpl {
    File scss
    File outDir
    ProjectAwareResolver resolver
    Boolean minify = false
    Boolean silent = false
    Boolean javafx = false


    def exec(){
        SCSSErrorHandler handler = silent? new SilentErrorHandler(): new SCSSErrorHandler()

        def sourceScss = scss

//        if ( javafx ) {
//            // create tempFile to preserve the original scss
//            sourceScss = new File('tmp_' + scss.getName())
//            sourceScss << scss.text
//
//            // Replace all null values with fake ones
//            sourceScss.write( sourceScss.text.replaceAll(':\\s*null\\s*;', ": $fakeValue;") )
//        }

        ScssStylesheet sass = ScssStylesheet.get(sourceScss.absolutePath, null, new SCSSDocumentHandlerImpl(),handler)
        sass.setFile(sourceScss)

        // setting charset breaks JavaFX CSS generation
        // and should be turned off for JavaFX applications
        if ( !javafx ) {
            sass.setCharset('UTF-8')
        }

        sass.addResolver(resolver.getFSResolver())
        sass.addResolver(resolver)
        def basename = scss.getName().replaceAll('\\.scss','.css')
        sass.compile()

        File file = new File(outDir.getAbsolutePath() + File.separator + basename)
        file.withWriter {
            sass.write(it, minify)
        }

        if (javafx) {
            // replace all fakeValues with nulls as it supposed to be
            file.write( file.text.replaceAll(":\\s*$fakeValue\\s*;", ': null;') )
//            sourceScss.delete() // delete temp file created only in javafx mode
        }
    }


}
