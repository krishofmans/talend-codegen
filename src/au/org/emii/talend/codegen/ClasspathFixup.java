package au.org.emii.talend.codegen;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.talend.core.model.genhtml.FileCopyUtils;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobExportReArchieveCreator;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;

/* Patch the zip file to use a classpath jar rather than specifying all jars in the classpath */
/* Refer org.talend.repository.ui.wizards.exportjob.action.reBuildJobZipFile */

public class ClasspathFixup {

    private JobJavaScriptsManager manager;

    public ClasspathFixup(JobJavaScriptsManager manager)
    {
        this.manager = manager;
    }

     /**
     *
     * DOC aiming Comment method "reBuildJobZipFile".
     *
     * @param processes
     */
    public void reBuildJobZipFile(List<ExportFileResource> processes, String zipFile, String projectDir) {

        JavaJobExportReArchieveCreator creator = new JavaJobExportReArchieveCreator(zipFile, "");
        String destinationZipFile = manager.getDestinationPath();
        String tmpFolder = creator.getTmpFolder();

        try {
            ZipToFile.unZipFile(zipFile, tmpFolder);

            // build new jar
            for (ExportFileResource process : processes) {
                if (process != null && process.getDirectoryName() != null) {
                    String jobFolderName = process.getDirectoryName();
                    int pos = jobFolderName.indexOf("/");
                    if (pos != -1) {
                        jobFolderName = jobFolderName.substring(pos + 1);
                    }

                    creator.setJobFolerName(jobFolderName);
                    creator.buildNewJar();

                    FileCopyUtils.copyFolder(String.format("%s/resources/%s", projectDir, jobFolderName), String.format("%s/%s", tmpFolder, jobFolderName));
                }
            }

            if (canCreateNewFile(destinationZipFile)) {
                ZipToFile.zipFile(tmpFolder, destinationZipFile);
            } else {
                System.out.println("Can not create a file or have not the permission to create a file! Does the destination directory exist?");
            }
        } catch (Exception e) {
            System.out.println("" + e);
        } finally {
            creator.deleteTempFiles();
            creator.deleteTempDestinationFiles();
            new File(zipFile).delete();
        }
    }

    /**
     * Added by Marvin Wang on Feb.1, 2012 for estimating if the file can be created. In win7 or other systems, have not
     * the permission to create a file directly under system disk(like C:\).
     *
     * @param disZipFileStr
     * @return
     */
    private boolean canCreateNewFile(String disZipFileStr) {
        boolean flag = true;
        File disZipFile = new File(disZipFileStr);
        if (!disZipFile.exists()) {
            try {
                disZipFile.getParentFile().mkdirs();
                disZipFile.createNewFile();
            } catch (IOException e) {
                flag = false;
            }
        }
        return flag;
    }
}
