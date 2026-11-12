package com.m2i.pti.service.dto;
import com.m2i.pti.domain.FileExport;
import com.m2i.pti.domain.FileExportParam;
import java.util.*;


public class FileExportDTO {
    private FileExport fileExport;
    private List<FileExportParam> params ;


    public FileExport getFileExport() {
        return this.fileExport;
    }

    public void setFileExport(FileExport fileExport) {
        this.fileExport = fileExport;
    }


    public List<FileExportParam> getParams() {
        return this.params;
    }

    public void setParams(List<FileExportParam> params) {
        this.params = params;
    }



    @Override
    public String toString() {
        return "{" +
            " fileExport='" + getFileExport() + "'" +
            ", params='" + getParams() + "'" +
            "}";
    }


}

