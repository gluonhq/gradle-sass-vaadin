package com.gluonhq.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.gluonhq.gradle.JavaFXTools.getFakeValue
/**
 * @author Radu Andries
 */
class SassCompileTask extends DefaultTask {

    @TaskAction
    def compileAllCss(){
        SassPluginExtension ext = project.getExtensions().getByName('sass') as SassPluginExtension;

        def sassFolder = ext.sassDir
        if ( ext.javafx) {

            // copy scssFile folder for preprocessing

            File tempDir = new File(getTemporaryDir(), UUID.randomUUID().toString())
            tempDir.deleteOnExit()

            def sTempDir = tempDir.getAbsolutePath()
            def sSassDir = new File(ext.sassDir).getCanonicalPath()

            def tree = project.fileTree(dir: sSassDir, include: '**/*.scss')
            tree.each {
                File tempFile = new File( it.getCanonicalPath().replaceFirst( sSassDir, sTempDir ) )
                tempFile.getParentFile().mkdirs()
                tempFile << it.text
                tempFile.write( tempFile.text.replaceAll(':\\s*null\\s*;', ": $fakeValue;") )
            }

            sassFolder = sTempDir

        }

        def tree = project.fileTree(dir: sassFolder, include: '**/*.scss')
        def resolv = new ProjectAwareResolver(project)
        File f = project.file(ext.cssDir)
        f.mkdirs()

        tree.each {

            if(it.name.startsWith('_')){
                //We should ignore this file.
                return
            }
            def scss = new SassCompilerImpl()
            scss.resolver = resolv
            scss.scssFile = it
            scss.scssDir = new File(sassFolder)
            scss.outDir = f
            scss.minify = ext.minify
            scss.silent = ext.silenceErrors
            scss.javafx = ext.javafx
            scss.exec()
        }
    }


}
