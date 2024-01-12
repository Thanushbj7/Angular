<!--How to display all cases related details using lightning component
Trying to display a list of records in the related list for the Case using lightning component.
I am getting the Case Id and displaying all the casesaction related to that object.
-->
<aura:component implements="flexipage:availableForAllPageTypes" access="global" controller = "CaseRelatedListApex" >
   

   <!--   <aura:attribute name="pageTitle" type="String" default="CASE HISTORY"/>-->
   <!--     <aura:attribute name="sessId" type="string"/>-->
    <aura:attribute name="cases" type="Case[]"/>
    <aura:handler name="init" value="{!this}" action="{!c.check}" />
   <!-- <aura:attribute name="Cases" type="list"/>-->
    <aura:attribute name="recordId" type="String" />
    <aura:attribute name="Casenumber" type="String" />
    <!--   <aura:handler event="c:CaseRelatedListEvent" action="{!c.handleCSDetailsEvent}" />-->
     <lightning:card title="CASE HISTORY" iconName="standard:add_contact">
    <div class="slds-col slds-col--padded slds-p-top--large">
 <div style="float: left; width: 20%; padding: 10px; ">
        <h1>{!v.pageTitle}</h1>
    </div>
   <div style="float: left; width: 80%; padding: 10px;">
   <table style="width: 100%; border-collapse: collapse;">
        <thead>
            <tr>
                <th style="border: 1px solid #ddd; padding: 8px;">Case Number</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Date Time</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Plan ID</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Inquiry</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Transactions</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Account Maintenance</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Forms</th>
                <th style="border: 1px solid #ddd; padding: 8px;">Other</th>
               
            </tr>
        </thead>
        <tbody>
            <aura:iteration items="{!v.cases}" var="caseRecord">
                
				 <!--<aura:if isTrue="{!caseRecord.Case__r.CaseNumber==Casenumber}">-->
                <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">{!caseRecord.Case__r.CaseNumber}</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">{!caseRecord.Case__r.CreatedDate}</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">{!caseRecord.PlanID_Text__c}</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">
                        <aura:if isTrue="{!caseRecord.Call_Activity__c=='Inquiry'}">
                            Inquiry
                        </aura:if>
                        
                    </td>
                    <td style="border: 1px solid #ddd; padding: 8px;">
                        <aura:if isTrue="{!caseRecord.Call_Activity__c=='Transactions'}">
                            Transactions
                        </aura:if>
                        
                    </td>
                     <td style="border: 1px solid #ddd; padding: 8px;">
                        <aura:if isTrue="{!caseRecord.Call_Activity__c=='Account Maintenance'}">
                            Account Maintenance
                        </aura:if>
                </td>
                    <td style="border: 1px solid #ddd; padding: 8px;">
                        <aura:if isTrue="{!caseRecord.Call_Activity__c=='Forms'}">
                            Forms
                        </aura:if>
                </td>
                    <!--assign case number-->
                </tr>
            </aura:iteration>
        </tbody>
    </table>
       
        </div>





	
        </div>
       
           </lightning:card>
    </aura:component>
For this code put a condition that casenumber should not repeat







	({
    check : function(component, event, helper) {
        // Existing code...
        
        // Initialize a set to store unique case numbers
        var uniqueCaseNumbers = new Set();

        // Iterate through the cases and add only unique case numbers
        var cases = component.get("v.cases");
        cases.forEach(function(caseRecord) {
            var caseNumber = caseRecord.Case__r.CaseNumber;
            if (!uniqueCaseNumbers.has(caseNumber)) {
                uniqueCaseNumbers.add(caseNumber);
            } else {
                // Case number already exists, handle accordingly (you can skip, alert, or take another action)
            }
        });

        // Update the component attribute with the unique cases
        component.set("v.cases", cases);
    }
})


({
    check : function(component, event, helper){
        console.log('inside controller');
        helper.getCases(component,event,helper);
    }
    ,
    
    handleClick : function(component,event,helper){
        helper.navigateTorecordDetails(component,event);
    }
})
