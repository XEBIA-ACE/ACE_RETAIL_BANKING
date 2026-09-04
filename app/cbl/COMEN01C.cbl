      ******************************************************************      
      * Program     : COMEN01C.CBL                                          *
      * Application : CardDemo                                              *
      * Type        : CICS Online Program                                   *
      * Function    : Customer Menu - Main Menu for Customer Users          *
      *                                                                     *
      * Change Log  :                                                       *
      *   2022-07-19  Initial version                                       *
      *   2026-03-10  TASK-041: Added dashboard menu option (option 6)      *
      *               to PROCESS-ENTER-KEY paragraph. New WHEN clause       *
      *               XCTLs to CODASH00C with COMMAREA. RESP checked per   *
      *               ISO-5055 rule 8162.                                   *
      ******************************************************************      
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    COMEN01C.
       AUTHOR.        AWS.
      ******************************************************************      
       ENVIRONMENT DIVISION.
      ******************************************************************      
       DATA DIVISION.
      ******************************************************************      
       WORKING-STORAGE SECTION.

       01  WS-PGMNAME                PIC X(8)   VALUE 'COMEN01C'.
       01  WS-TRANID                 PIC X(4)   VALUE 'CM00'.

       01  WS-MESSAGE                PIC X(80)  VALUE SPACES.
       01  WS-ERRFLG                 PIC X(1)   VALUE SPACES.
       01  WS-RESP                   PIC S9(9)  COMP VALUE ZEROS.
       01  WS-RESP2                  PIC S9(9)  COMP VALUE ZEROS.

       01  WS-OPTION                 PIC X(2)   VALUE SPACES.

       01  WS-COMMAREA-LEN           PIC S9(4)  COMP VALUE +0.

      ******************************************************************      
      *    INCLUDE COPYBOOKS                                                *
      ******************************************************************      
           COPY COCOM01Y.
           COPY COTTL01Y.
           COPY CSDAT01Y.
           COPY CSMSG01Y.
           COPY CSUSR01Y.
           COPY DFHAID.
           COPY DFHBMSCA.
           COPY COMEN01Y.

       01  CARDDEMO-MAIN-MENU-OPTIONS.
           05  CARDDEMO-MENU-OPT-COUNT     PIC 9(2)   VALUE 6.
           05  CARDDEMO-MENU-OPT-ITEMS.
               10  CARDDEMO-MENU-OPT1      PIC X(40)
                   VALUE '1 - View Account and Transaction'.
               10  CARDDEMO-MENU-OPT2      PIC X(40)
                   VALUE '2 - Update Account Information'.
               10  CARDDEMO-MENU-OPT3      PIC X(40)
                   VALUE '3 - Payment Processing'.
               10  CARDDEMO-MENU-OPT4      PIC X(40)
                   VALUE '4 - Report Generation'.
               10  CARDDEMO-MENU-OPT5      PIC X(40)
                   VALUE '5 - Manage Credit Cards'.
      *----------------------------------------------------------------*
      * TASK-041: Option 6 added for Consolidated Account Dashboard    *
      *----------------------------------------------------------------*
               10  CARDDEMO-MENU-OPT6      PIC X(40)
                   VALUE '6 - View Account Dashboard'.
           05  CARDDEMO-MENU-OPT9          PIC X(40)
                   VALUE '9 - Sign Off'.

      ******************************************************************      
       LINKAGE SECTION.
       01  DFHCOMMAREA.
           05  LK-COMMAREA             PIC X(1)
                                       OCCURS 1 TO 32767 TIMES
                                       DEPENDING ON EIBCALEN.

      ******************************************************************      
       PROCEDURE DIVISION.
      ******************************************************************      
       MAIN-PARA.
      ******************************************************************      
      *    Main processing paragraph                                        *
      ******************************************************************      
           MOVE SPACES TO WS-MESSAGE
           MOVE SPACES TO WS-ERRFLG

           IF EIBCALEN = 0
               MOVE 'NO COMMAREA - XCTL TO SIGNON' TO WS-MESSAGE
               PERFORM RETURN-TO-SIGNON-SCREEN
           END-IF

           MOVE EIBCALEN TO WS-COMMAREA-LEN

           MOVE DFHCOMMAREA (1:EIBCALEN) TO WS-COMMAREA

           IF CDEMO-PGM-REENTER
               PERFORM PROCESS-ENTER-KEY
           ELSE
               PERFORM SEND-MENU-SCREEN
           END-IF

           PERFORM COMMON-RETURN.

      ******************************************************************      
       PROCESS-ENTER-KEY.
      ******************************************************************      
      *    Process the ENTER key - evaluate the menu option selected        *
      ******************************************************************      
           MOVE SPACES TO WS-ERRFLG

           EXEC CICS RECEIVE MAP('COMEN1A')
                             MAPSET('COMEN01')
                             INTO(COMEN1AI)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
               MOVE 'ERROR RECEIVING MAP COMEN1A' TO WS-MESSAGE
               MOVE 'Y'                           TO WS-ERRFLG
               PERFORM SEND-MENU-SCREEN
               GO TO PROCESS-ENTER-KEY-EXIT
           END-IF

           MOVE OPTIONI OF COMEN1AI TO WS-OPTION

           EVALUATE WS-OPTION
      ******************************************************************      
      *        Option 1 - View Account and Transaction                      *
      ******************************************************************      
               WHEN '01'
               WHEN ' 1'
               WHEN '1 '
                   MOVE 'COACTVWC' TO CDEMO-TO-PROGRAM
                   MOVE 'CAVW'     TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C' TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'     TO CDEMO-FROM-TRANID
                   MOVE 0          TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('COACTVWC')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO COACTVWC' TO WS-MESSAGE
                       MOVE 'Y'                      TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 2 - Update Account Information                        *
      ******************************************************************      
               WHEN '02'
               WHEN ' 2'
               WHEN '2 '
                   MOVE 'COACTUPC' TO CDEMO-TO-PROGRAM
                   MOVE 'CAUP'     TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C' TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'     TO CDEMO-FROM-TRANID
                   MOVE 0          TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('COACTUPC')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO COACTUPC' TO WS-MESSAGE
                       MOVE 'Y'                      TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 3 - Payment Processing                                *
      ******************************************************************      
               WHEN '03'
               WHEN ' 3'
               WHEN '3 '
                   MOVE 'COBIL00C' TO CDEMO-TO-PROGRAM
                   MOVE 'CBIL'     TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C' TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'     TO CDEMO-FROM-TRANID
                   MOVE 0          TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('COBIL00C')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO COBIL00C' TO WS-MESSAGE
                       MOVE 'Y'                      TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 4 - Report Generation                                 *
      ******************************************************************      
               WHEN '04'
               WHEN ' 4'
               WHEN '4 '
                   MOVE 'CORPT00C' TO CDEMO-TO-PROGRAM
                   MOVE 'CRPT'     TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C' TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'     TO CDEMO-FROM-TRANID
                   MOVE 0          TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('CORPT00C')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO CORPT00C' TO WS-MESSAGE
                       MOVE 'Y'                      TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 5 - Manage Credit Cards                               *
      ******************************************************************      
               WHEN '05'
               WHEN ' 5'
               WHEN '5 '
                   MOVE 'COCRDSLC' TO CDEMO-TO-PROGRAM
                   MOVE 'CCSL'     TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C' TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'     TO CDEMO-FROM-TRANID
                   MOVE 0          TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('COCRDSLC')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO COCRDSLC' TO WS-MESSAGE
                       MOVE 'Y'                      TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 6 - View Account Dashboard                            *
      *        TASK-041: New WHEN clause for consolidated dashboard.        *
      *        Issues XCTL to CODASH00C passing COMMAREA and LENGTH.       *
      *        WS-RESP is checked immediately after the XCTL call per      *
      *        ISO-5055 rule 8162. No existing WHEN clause is altered.     *
      ******************************************************************      
               WHEN '06'
               WHEN ' 6'
               WHEN '6 '
                   MOVE 'CODASH00C' TO CDEMO-TO-PROGRAM
                   MOVE 'CD00'      TO CDEMO-TO-TRANID
                   MOVE 'COMEN01C'  TO CDEMO-FROM-PROGRAM
                   MOVE 'CM00'      TO CDEMO-FROM-TRANID
                   MOVE 0           TO CDEMO-PGM-CONTEXT
                   EXEC CICS XCTL
                             PROGRAM('CODASH00C')
                             COMMAREA(WS-COMMAREA)
                             LENGTH(WS-COMMAREA-LEN)
                             RESP(WS-RESP)
                             RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
                       MOVE 'ERROR XCTL TO CODASH00C' TO WS-MESSAGE
                       MOVE 'Y'                       TO WS-ERRFLG
                       PERFORM SEND-MENU-SCREEN
                   END-IF
      ******************************************************************      
      *        Option 9 - Sign Off                                          *
      ******************************************************************      
               WHEN '09'
               WHEN ' 9'
               WHEN '9 '
                   PERFORM RETURN-TO-SIGNON-SCREEN
      ******************************************************************      
      *        Invalid option entered                                        *
      ******************************************************************      
               WHEN OTHER
                   MOVE 'INVALID OPTION SELECTED - PLEASE TRY AGAIN'
                       TO WS-MESSAGE
                   MOVE 'Y' TO WS-ERRFLG
                   PERFORM SEND-MENU-SCREEN
           END-EVALUATE

       PROCESS-ENTER-KEY-EXIT.
           EXIT.

      ******************************************************************      
       SEND-MENU-SCREEN.
      ******************************************************************      
      *    Build and send the customer menu screen                          *
      ******************************************************************      
           PERFORM BUILD-MENU-OPTIONS

           MOVE WS-PGMNAME TO CDEMO-FROM-PROGRAM
           MOVE WS-TRANID   TO CDEMO-FROM-TRANID
           MOVE 1           TO CDEMO-PGM-REENTER

           EXEC CICS SEND MAP('COMEN1A')
                          MAPSET('COMEN01')
                          FROM(COMEN1AO)
                          ERASE
                          RESP(WS-RESP)
                          RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
               MOVE 'ERROR SENDING MAP COMEN1A' TO WS-MESSAGE
               PERFORM ABEND-ROUTINE
           END-IF.

      ******************************************************************      
       BUILD-MENU-OPTIONS.
      ******************************************************************      
      *    Populate the menu option fields on the COMEN1A map               *
      *    TASK-040/TASK-041: Option 6 (View Account Dashboard) added.     *
      *    All existing option numbers are unchanged.                       *
      ******************************************************************      
           MOVE CARDDEMO-MENU-OPT1 TO OPT1O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT2 TO OPT2O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT3 TO OPT3O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT4 TO OPT4O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT5 TO OPT5O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT6 TO OPT6O  OF COMEN1AO
           MOVE CARDDEMO-MENU-OPT9 TO OPT9O  OF COMEN1AO

           IF WS-ERRFLG = 'Y'
               MOVE WS-MESSAGE TO ERRMSGO OF COMEN1AO
           ELSE
               MOVE SPACES     TO ERRMSGO OF COMEN1AO
           END-IF

           MOVE CDEMO-CUST-FNAME TO FNAMEO  OF COMEN1AO
           MOVE CDEMO-CUST-LNAME TO LNAMEO  OF COMEN1AO
           MOVE CDEMO-CUST-ID    TO CUSIDOO OF COMEN1AO.

      ******************************************************************      
       COMMON-RETURN.
      ******************************************************************      
      *    Return to CICS with COMMAREA to maintain session state           *
      ******************************************************************      
           MOVE WS-PGMNAME TO CDEMO-FROM-PROGRAM
           MOVE WS-TRANID   TO CDEMO-FROM-TRANID

           EXEC CICS RETURN
                     TRANSID(WS-TRANID)
                     COMMAREA(WS-COMMAREA)
                     LENGTH(WS-COMMAREA-LEN)
                     RESP(WS-RESP)
                     RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

      ******************************************************************      
       RETURN-TO-SIGNON-SCREEN.
      ******************************************************************      
      *    Transfer control back to the sign-on program                     *
      ******************************************************************      
           EXEC CICS XCTL
                     PROGRAM('COSGN00C')
                     RESP(WS-RESP)
                     RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT EQUAL DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

      ******************************************************************      
       ABEND-ROUTINE.
      ******************************************************************      
      *    Abnormal termination handler                                     *
      ******************************************************************      
           EXEC CICS ABEND
                     ABCODE('CM01')
           END-EXEC.