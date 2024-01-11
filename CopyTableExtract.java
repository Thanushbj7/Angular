using System;
using System.IO;
using iText.Kernel.Pdf;
using Xceed.Words.NET;

class DataExporter
{
    private const string PdfOutputPath = "pdf_output.txt";
    private const string WordOutputPath = "word_output.txt";

    static void Main()
    {
        // Extract data from PDF
        string pdfData = ExtractPdfData("your_pdf_file.pdf");
        ExportDataToField(pdfData, PdfOutputPath);

        // Extract data from Word
        string wordData = ExtractWordData("your_word_file.docx");
        ExportDataToField(wordData, WordOutputPath);
    }

    private static string ExtractPdfData(string pdfFilePath)
    {
        using (PdfReader pdfReader = new PdfReader(pdfFilePath))
        {
            using (PdfDocument pdfDocument = new PdfDocument(pdfReader))
            {
                return new PdfTextExtractor(pdfDocument).GetText();
            }
        }
    }

    private static string ExtractWordData(string wordFilePath)
    {
        using (DocX document = DocX.Load(wordFilePath))
        {
            return document.Text;
        }
    }

    private static void ExportDataToField(string data, string outputPath)
    {
        try
        {
            File.WriteAllText(outputPath, data);
            Console.WriteLine($"Data exported to: {outputPath}");
        }
        catch (IOException e)
        {
            Console.WriteLine($"Error exporting data to {outputPath}: {e.Message}");
        }
    }
}
