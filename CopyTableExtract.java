<aura:component implements="flexipage:availableForAllPageTypes" access="global" controller="CaseRelatedListApex">
    <aura:attribute name="cases" type="Case[]" />
    <aura:attribute name="recordId" type="String" />
    <aura:attribute name="Casenumber" type="String" />
    <aura:attribute name="uniqueCaseNumbers" type="Object" default="{}" />

    <aura:handler name="init" value="{!this}" action="{!c.check}" />

    <lightning:card title="CASE HISTORY" iconName="standard:add_contact">
        <div class="slds-col slds-col--padded slds-p-top--large">
            <div style="float: left; width: 20%; padding: 10px;">
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
                            <!-- Check if casenumber is not in the set to avoid repetition -->
                            <aura:if isTrue="{!v.uniqueCaseNumbers[caseRecord.Case__r.CaseNumber] !== true}">
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
                                </tr>
                                <!-- Mark casenumber as encountered to avoid repetition -->
                                <aura:set attribute="v.uniqueCaseNumbers[caseRecord.Case__r.CaseNumber]" value="true" />
                            </aura:if>
                        </aura:iteration>
                    </tbody>
                </table>
            </div>
        </div>
    </lightning:card>
</aura:component>
