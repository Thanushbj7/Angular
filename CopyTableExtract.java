Public with sharing class CaseRelatedListApex{
   
    @AuraEnabled(cacheable=true)
    public static List<Case_Actions__c> getObject(){  
        LIST< Case_Actions__c > objCaseList = new LIST< Case_Actions__c >(); 
        
        objCaseList =[SELECT  Case__r.CaseNumber,Case__r.CreatedDate,PlanID_Text__c,Call_Activity__c,Call_Type__c FROM Case_Actions__c where  Case__r.Account.SSN__c IN  ('010820241')];
        
        return  objCaseList;
        
          }
    public static List<Case_Actions__c> getcaseactionrecords(){
        
        List<Case_Actions__c> Caseaction = new List<Case_Actions__c>();
       // if((String.isBlank(Caseid) || Caseid==''||Caseid==null) &&(String.isBlank(clientStatus) || clientStatus==''||clientStatus==null)){
            Caseaction = [SELECT  Case__r.CaseNumber,Case__r.CreatedDate,PlanID_Text__c,Call_Activity__c,Call_Type__c 
                               from Case_Actions__c where  Case__r.Account.SSN__c IN  ('010820241') ];
       
       // }
        
        return generateWrapperData(Caseaction);
    }
    
    private static List<Case_Actions__c> generateWrapperData(List<Case_Actions__c> Caseactionlist) {
        List<Case_Actions__c> caseactionWrapperList = new List<Case_Actions__c>();
        
        for (Case_Actions__c Caction : Caseactionlist) {
            
            Case_Actions__c CactionWrap = new Case_Actions__c();
            
            CactionWrap.id = Caction.Id;
            CactionWrap.PlanID_Text__c = Caction.PlanID_Text__c;
            CactionWrap.Call_Activity__c = Caction.Call_Activity__c;
            CactionWrap.Call_Type__c = Caction.Call_Type__c;          
         
            caseactionWrapperList.add(CactionWrap); 
        }
        
        return caseactionWrapperList;
    }
    
}














import { LightningElement,api,track,wire } from 'lwc';
import { publish, MessageContext } from 'lightning/messageService';
import EXAMPLE_MESSAGE_CHANNEL from '@salesforce/messageChannel/ExampleMessageChannel__c';
import getObject from '@salesforce/apex/CaseRelatedListApex.getObject';
//import getcaseactionrecords from '@salesforce/apex/CaseRelatedListApex.getcaseactionrecords';

const columns = [
    { label: 'Case Number', fieldName: 'CaseNumber' },
    { label: 'Date', fieldName: 'CreatedDate' },
    { label: 'Plan Id', fieldName: 'PlanID_Text__c' },
    { label: 'Inquiry', fieldName: 'Call_Type__c' },
    { label: 'Transactions', fieldName: 'Call_Type__c' },
		 { label: 'Account Maintenance', fieldName: 'Call_Type__c' },
		 { label: 'Forms', fieldName: 'Call_Type__c'},
		{ label: 'Others', fieldName: 'Call_Type__c'},
];

export default class CaseHistoryLWC extends LightningElement {
		@track passedValue;
		dnisNumber;
		source;
		CTIP
		clientSSN;
		ctiVRUApp;
		ctiEDU;
		AuthenticatedFlag;
		caseOrigin;

		@wire(MessageContext)
		messageContext;
		
		@track data = [];
    @track columns = columns;
    wiredRecords;
		@wire(getObject) wiredCases(value){
			this.wiredRecords=value;
				const {data, error}= value;
				if(data){
						
					let tempRecords= JSON.parse(JSON.stringify(data));
						
							 tempRecords= tempRecords.map(row=>{
									
												 return {...row ,CaseNumber:row.Case__r.CaseNumber , CreatedDate:row.Case__r.CreatedDate};
									 
							 });
						this.data= tempRecords;
							console.log("tempRecords!" , tempRecords);
				}
				if(error){
						console.log("error Occurred!" , error);
				}
		}
    

		connectedCallback() {
				// Parse the URL and get the 'passedValue' parameter
				const urlParams = new URLSearchParams(window.location.search);
				this.passedValue = urlParams.get('passedValue') || 'No value passed';
				this.Id = urlParams.get('Id');		
				this.source = urlParams.get('source');
				this.clientSSN = urlParams.get('clientSSN');			
				this.AuthenticatedFlag = urlParams.get('AuthenticatedFlag');
				this.caseOrigin = urlParams.get('caseOrigin');
				
				console.log('Line 31 '+'Id '+this.Id+this.source+ this.clientSSN +this.AuthenticatedFlag+this.caseOrigin );
				/*getPlans({ Id:this.Id, clientSSN:this.clientSSN, caseOrigin:this.caseOrigin})
						.then(result => {
						// Handle the returned list in the result
						console.log('Returned List:', result);
				})
						.catch(error => {
						// Handle any errors here
						console.error('Error:', error);
				});*/
 

				try{
						const MSG = {
								message: 'Your message here'
						};
						// publish(this.messageContext, EXAMPLE_MESSAGE_CHANNEL, xyz);
				}
				catch(error){
						console.error('Error publishing message', error);
				}

		}

		sendMessage() {
				try {
						const message = {
								message: 'Your message here'
						};
						publish(this.messageContext, EXAMPLE_MESSAGE_CHANNEL, message);
				} catch (error) {
						console.error('Error publishing message', error);
				}
		}


}






// ... Existing imports ...

const columns = [
    { label: 'Case Number', fieldName: 'CaseNumber' },
    { label: 'Date', fieldName: 'CreatedDate' },
    { label: 'Plan Id', fieldName: 'PlanID_Text__c' },
    { label: 'Inquiry', fieldName: 'Inquiry' },
    { label: 'Transactions', fieldName: 'Transactions' },
    { label: 'Account Maintenance', fieldName: 'AccountMaintenance' },
    { label: 'Forms', fieldName: 'Forms' },
    { label: 'Others', fieldName: 'Others' },
];

export default class CaseHistoryLWC extends LightningElement {
    // ... Existing properties and methods ...

    @wire(getObject) wiredCases(value){
        // ... Existing wire method code ...

        if (data) {
            let tempRecords = JSON.parse(JSON.stringify(data));
            
            tempRecords = tempRecords.map(row => {
                let callTypes = row.Call_Type__c ? row.Call_Type__c.split(';') : [];
                let isFirstRow = true;

                return callTypes.reduce((acc, callType) => {
                    // The first row will have the original data, subsequent rows will be empty
                    const newRow = {
                        ...row,
                        CaseNumber: isFirstRow ? row.Case__r.CaseNumber : '',
                        CreatedDate: isFirstRow ? row.Case__r.CreatedDate : '',
                        Inquiry: isFirstRow ? callType : '',
                        Transactions: isFirstRow ? '' : '',
                        AccountMaintenance: isFirstRow ? '' : '',
                        Forms: isFirstRow ? '' : '',
                        Others: isFirstRow ? '' : '',
                    };

                    acc.push(newRow);
                    isFirstRow = false;
                    return acc;
                }, []);
            });

            this.data = tempRecords.flat();
            console.log("tempRecords!", tempRecords);
        }

        // ... Existing error handling ...
    }

    // ... Existing methods ...
	}
