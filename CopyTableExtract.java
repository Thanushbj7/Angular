
Public class CaseRelatedListApex{
    @AuraEnabled
    public static List<Case_Actions__c> getObject(){
       // LIST< session__c > objsessList = new LIST< session__c >();
        LIST< Case_Actions__c > objCaseList = new LIST< Case_Actions__c >();
             
        
          //   if(sessId!=null)
          //where session_ID__c =:sessId
      
           objCaseList =[SELECT  Case__r.CaseNumber,Case__r.CreatedDate,PlanID_Text__c,Call_Activity__c FROM Case_Actions__c where  Call_Activity__c IN('Inquiry','Transactions','Account Maintenance','Forms')   and Case__r.Account.SSN__c IN  ('010820241')  ];
            //return  objCaseList;       
           
     
         return  objCaseList;
        }
       
        
}
