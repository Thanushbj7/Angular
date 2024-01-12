public class CaseRelatedListApex {
    public class CaseWrapper {
        @AuraEnabled
        public String caseNumber { get; set; }

        public CaseWrapper(String caseNumber) {
            this.caseNumber = caseNumber;
        }
    }

    @AuraEnabled
    public static List<CaseWrapper> getUniqueCaseNumbers() {
        List<CaseWrapper> uniqueCaseNumbersList = new List<CaseWrapper>();

        // Perform a SOQL query to fetch Case_Actions__c records
        List<Case_Actions__c> caseActionsList = [SELECT Case__r.CaseNumber FROM Case_Actions__c WHERE Call_Activity__c IN ('Inquiry', 'Transactions', 'Account Maintenance', 'Forms') AND Case__r.Account.SSN__c = '010820241'];

        // Use a Set to track unique Case Numbers
        Set<String> uniqueCaseNumbersSet = new Set<String>();

        // Iterate through the retrieved Case_Actions__c records
        for (Case_Actions__c caseAction : caseActionsList) {
            String caseNumber = caseAction.Case__r.CaseNumber;

            // Check if the Case Number is not already in the set
            if (!uniqueCaseNumbersSet.contains(caseNumber)) {
                uniqueCaseNumbersSet.add(caseNumber);

                // Create a new instance of the wrapper class and add it to the list
                uniqueCaseNumbersList.add(new CaseWrapper(caseNumber));
            }
        }

        return uniqueCaseNumbersList;
    }
          }
