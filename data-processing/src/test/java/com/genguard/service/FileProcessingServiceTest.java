//package com.genguard.service;
//
//import com.genguard.utils.VcfUtils;
//import org.snpeff.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.snpeff.snpEffect.Config;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class FileProcessingServiceTest {
//
//    @Mock
//    private VcfUtils vcfUtils;
//
//    @InjectMocks
//    private FileProcessingService fileProcessingService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testProcessFile_Success() throws IOException {
//        // Mock input MultipartFile
//        MockMultipartFile inputFile = new MockMultipartFile(
//                "file",
//                "test.vcf",
//                "text/plain",
//                "dummy vcf content".getBytes()
//        );
//
//        // Mock VcfUtils behavior
//        when(vcfUtils.getGenomeBuild(any(File.class))).thenReturn("hg19");
//
//        // Call the processFile method
//        fileProcessingService.processFile(inputFile);
//
//        // Verify temporary files created and processed
//        ArgumentCaptor<File> tempFileCaptor = ArgumentCaptor.forClass(File.class);
//        verify(vcfUtils, atLeastOnce()).getGenomeBuild(tempFileCaptor.capture());
//
//        // Check result data is populated
//        assertNotNull(fileProcessingService.getResultAsMultiPartFile());
//        verifyNoMoreInteractions(vcfUtils);
//    }
//
//    @Test
//    void testProcessFile_Failure() throws IOException {
//        // Mock input MultipartFile
//        MockMultipartFile inputFile = new MockMultipartFile(
//                "file",
//                "test.vcf",
//                "text/plain",
//                "dummy vcf content".getBytes()
//        );
//
//        // Mock the behavior of the transferTo method to throw an IOException
//        MockMultipartFile spyFile = spy(inputFile);
//        doThrow(new IOException("Test exception")).when(spyFile).transferTo(any(File.class));
//
//        // Call processFile and verify it throws the exception
//        IOException thrownException = assertThrows(IOException.class, () -> {
//            fileProcessingService.processFile(spyFile);
//        });
//
//        // Verify the exception is logged and thrown
//        assertEquals("Test exception", thrownException.getMessage());
//    }
//
//    @Test
//    void testAnnotateAndFilter() throws IOException {
//        // Mock behavior for internal annotateAndFilter logic
//        File mockInputFile = mock(File.class);
//        File mockOutputFile = Files.createTempFile("mock_output", ".vcf").toFile();
//        when(vcfUtils.getGenomeBuild(mockInputFile)).thenReturn("hg19");
//
//        File resultFile = fileProcessingService.annotateAndFilter(mockInputFile);
//
//        // Verify the resulting file is not null
//        assertNotNull(resultFile);
//        assertTrue(resultFile.exists());
//
//        // Clean up
//        mockOutputFile.delete();
//    }
//
//    @Test
//    void testGetResultAsMultiPartFile() throws IOException {
//        // Set up result data
//        byte[] mockData = "mock result content".getBytes();
//        ReflectionTestUtils.setField(fileProcessingService, "resultFileData", mockData);
//
//        // Call getResultAsMultiPartFile
//        MockMultipartFile result = (MockMultipartFile) fileProcessingService.getResultAsMultiPartFile();
//
//        // Verify the result file's properties
//        assertNotNull(result);
//        assertEquals("processedDataFile_" + System.currentTimeMillis() + ".vcf", result.getOriginalFilename());
//        assertArrayEquals(mockData, result.getBytes());
//    }
//    @Test
//    void testProcessFileWithRealVcf() throws IOException {
//        // Path to your real VCF file for testing
//        File realVcfFile = new File("/Users/anatolyizotov/IdeaProjects/geneguard/snpEff/examples/variants_1.ann.vcf");
//
//        // Ensure the file exists before proceeding
//        assertTrue(realVcfFile.exists(), "Test VCF file does not exist at the given path.");
//
//        // Read the VCF file data into a byte array
//        byte[] vcfData = Files.readAllBytes(realVcfFile.toPath());
//
//        // Create a MockMultipartFile from the real VCF data
//        MockMultipartFile inputFile = new MockMultipartFile(
//                "file",
//                "variants_1.ann.vcf",
//                "text/plain",
//                vcfData
//        );
//
//        // Mock the genome build retrieval. Adjust the genome build if needed for your environment.
//        when(vcfUtils.getGenomeBuild(any(File.class))).thenReturn("GRCh37");
//
//        // Process the real file
//        fileProcessingService.processFile(inputFile);
//
//        // Retrieve the processed result
//        MultipartFile result = fileProcessingService.getResultAsMultiPartFile();
//
//        // Assertions
//        assertNotNull(result, "The resulting file should not be null.");
//        //assertTrue(result.getSize() > 0, "The resulting file should not be empty.");
//
//        // Optionally, you can read the result content and verify it contains expected annotations
//        // For example, check if the file has at least one annotated line:
//        String resultContent = new String(result.getBytes());
//        assertTrue(resultContent.contains("EFF="), "The result file should contain annotation effects.");
//    }
//}