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






<!-- MyObjectComponent.cmp -->
<aura:component controller="MyObjectController">
    <aura:attribute name="picklistValues" type="List" />

    <!-- Initialization -->
    <aura:handler name="init" value="{!this}" action="{!c.doInit}" />

    <!-- Table Section -->
    <table style="width: 100%; border-collapse: collapse;">
        <thead>
            <tr>
                <th style="border: 1px solid #ddd; padding: 8px;">Picklist Values</th>
            </tr>
        </thead>
        <tbody>
            <aura:iteration items="{!v.picklistValues}" var="value">
                <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">{!value}</td>
                </tr>
            </aura:iteration>
        </tbody>
    </table>
</aura:component>




    {!v.excludedCallActivities.includes(action.Call_Activity__c) ? action.Call_Activity__c : ''}

    SELECT Case__r.CaseNumber,Date_Time_c__c,PlanID_Text__c,Call_Activity__c,Call_Type__c FROM Case_Actions__c where Call_Type__c IN ('Other') AND Call_Activity__c IN('Inquiry','Transactions','Account Maintenance','Forms')   and Case__r.Account.SSN__c in  ('010820241')];
            
