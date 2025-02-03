package com.genguard.service;

import java.io.File;
import java.io.IOException;

public interface DefaultVcfParserService {
    void parseVcfFile(File annotatedFile) throws IOException;
}
