@ui @dynamic-data @ignore
Feature: Test des donnees dynamiques

  Background:
    * log in to the application
    * search locators in the home_page
    * click on btn_my_account
    * search locators in the login_page


  Scenario: Tester la fonction today
    * write ${today:} in field txt_email
    * save value of txt_email for later in variable GeneratedToday
    * assert value of that element txt_email contains ${GeneratedToday}


  Scenario: Tester la fonction todayISO
    * write REF-${todayISO:} in field txt_email
    * save value of txt_email for later in variable GeneratedTodayISO
    * assert value of that element txt_email contains ${GeneratedTodayISO}


  Scenario: Tester la generation de chaine alphabetique
    * write ${string:8,alpha} in field txt_email
    * save value of txt_email for later in variable GeneratedAlphaString
    * assert value of that element txt_email contains ${GeneratedAlphaString}


  Scenario: Tester la generation de chaine numerique
    * write ${string:10,numeric} in field txt_email
    * save value of txt_email for later in variable GeneratedNumericString
    * assert value of that element txt_email contains ${GeneratedNumericString}


  Scenario: Tester la generation de chaine alphanumerique
    * write ${string:12,alnum} in field txt_email
    * save value of txt_email for later in variable GeneratedAlnumString
    * assert value of that element txt_email contains ${GeneratedAlnumString}


  Scenario: Tester la generation de date de naissance
    * write ${birthDate:21,45} in field txt_email
    * save value of txt_email for later in variable GeneratedBirthDate
    * assert value of that element txt_email contains ${GeneratedBirthDate}


  Scenario: Tester la fonction siren
    * write ${siren:} in field txt_email
    * save value of txt_email for later in variable GeneratedSiren
    * assert value of that element txt_email contains ${GeneratedSiren}


  Scenario: Tester la fonction siret
    * write ${siret:} in field txt_email
    * save value of txt_email for later in variable GeneratedSiret
    * assert value of that element txt_email contains ${GeneratedSiret}


  Scenario: Tester la fonction nirpp avec une date fixe
    * write ${nirpp:1,12/03/1997} in field txt_email
    * save value of txt_email for later in variable GeneratedNirpp
    * assert value of that element txt_email contains ${GeneratedNirpp}


  Scenario: Tester la fonction nirpp avec une date generee
    * write ${birthDate:21,45} in field txt_email
    * save value of txt_email for later in variable GeneratedBirthDateForNirpp
    * write ${nirpp:1,${GeneratedBirthDateForNirpp}} in field txt_email
    * save value of txt_email for later in variable GeneratedNirppFromBirthDate
    * assert value of that element txt_email contains ${GeneratedNirppFromBirthDate}


  Scenario: Tester la fonction firstDayOfYear
    * write ${firstDayOfYear:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedFirstDayOfYear
    * assert value of that element txt_email contains ${GeneratedFirstDayOfYear}


  Scenario: Tester la fonction lastDayOfYear
    * write ${lastDayOfYear:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedLastDayOfYear
    * assert value of that element txt_email contains ${GeneratedLastDayOfYear}


  Scenario: Tester la fonction firstDayOfMonth
    * write ${firstDayOfMonth:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedFirstDayOfMonth
    * assert value of that element txt_email contains ${GeneratedFirstDayOfMonth}


  Scenario: Tester la fonction lastDayOfMonth
    * write ${lastDayOfMonth:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedLastDayOfMonth
    * assert value of that element txt_email contains ${GeneratedLastDayOfMonth}


  Scenario: Tester la fonction shiftDate avec jours
    * write ${shiftDate:28/05/2026,10,days} in field txt_email
    * save value of txt_email for later in variable GeneratedShiftDateDays
    * assert value of that element txt_email contains ${GeneratedShiftDateDays}


  Scenario: Tester la fonction shiftDate avec mois
    * write ${shiftDate:28/05/2026,-1,months} in field txt_email
    * save value of txt_email for later in variable GeneratedShiftDateMonths
    * assert value of that element txt_email contains ${GeneratedShiftDateMonths}


  Scenario: Tester la fonction shiftDate avec annees
    * write ${shiftDate:28/05/2026,1,years} in field txt_email
    * save value of txt_email for later in variable GeneratedShiftDateYears
    * assert value of that element txt_email contains ${GeneratedShiftDateYears}


  Scenario: Tester la fonction formatDate
    * write ${formatDate:28/05/2026,yyyyMMdd} in field txt_email
    * save value of txt_email for later in variable GeneratedFormattedDate
    * assert value of that element txt_email contains ${GeneratedFormattedDate}


  Scenario: Tester la fonction parseDate
    * write ${parseDate:2026-05-28,yyyy-MM-dd} in field txt_email
    * save value of txt_email for later in variable GeneratedParsedDate
    * assert value of that element txt_email contains ${GeneratedParsedDate}


  Scenario: Tester la fonction lastDayOfPreviousMonth
    * write ${lastDayOfPreviousMonth:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedLastDayOfPreviousMonth
    * assert value of that element txt_email contains ${GeneratedLastDayOfPreviousMonth}


  Scenario: Tester la fonction firstDayOfNextMonth
    * write ${firstDayOfNextMonth:28/05/2026} in field txt_email
    * save value of txt_email for later in variable GeneratedFirstDayOfNextMonth
    * assert value of that element txt_email contains ${GeneratedFirstDayOfNextMonth}


  Scenario: Tester la fonction toUpperCase
    * write ${toUpperCase:client demo} in field txt_email
    * save value of txt_email for later in variable GeneratedUpperCase
    * assert value of that element txt_email contains ${GeneratedUpperCase}


  Scenario: Tester la fonction toLowerCase
    * write ${toLowerCase:CLIENT DEMO} in field txt_email
    * save value of txt_email for later in variable GeneratedLowerCase
    * assert value of that element txt_email contains ${GeneratedLowerCase}


  Scenario: Tester la fonction extractSubstring
    * write ${extractSubstring:Hello World,6,5} in field txt_email
    * save value of txt_email for later in variable GeneratedSubstring
    * assert value of that element txt_email contains ${GeneratedSubstring}


  Scenario: Tester la fonction sys
    * write ${sys:user.dir} in field txt_email
    * save value of txt_email for later in variable GeneratedSystemProperty
    * assert value of that element txt_email contains ${GeneratedSystemProperty}


  Scenario: Tester la fonction add
    * write ${add:1200,300} in field txt_email
    * save value of txt_email for later in variable GeneratedAddition
    * assert value of that element txt_email contains ${GeneratedAddition}


  Scenario: Tester la fonction extractAmount
    * write ${extractAmount:1 234.99 EUR} in field txt_email
    * save value of txt_email for later in variable GeneratedAmount
    * assert value of that element txt_email contains ${GeneratedAmount}


  Scenario: Tester une reference composee avec plusieurs fonctions
    * write CLIENT-${string:5,alpha}-${todayISO:} in field txt_email
    * save value of txt_email for later in variable GeneratedClientReference
    * assert value of that element txt_email contains ${GeneratedClientReference}