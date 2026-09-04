      ******************************************************************
      * Program     : COADM01C
      * Application : CardDemo
      * Type        : CICS Online Program
      * Function    : Admin Menu — display options and dispatch to
      *               selected function.
      *
      * Modification History:
      *   TASK-042 / TASK-043 — Added "View Account Dashboard" option
      *               (option 9) to BUILD-MENU-OPTIONS and added
      *               corresponding WHEN clause in PROCESS-ENTER-KEY
      *               to XCTL to CODASH00C.  No existing option numbers
      *               were changed.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    COADM01C.
       AUTHOR.        ACME MAINFRAME TEAM.
       DATE-WRITTEN.  2024-01-01.

      ******************************************************************
       ENVIRONMENT DIVISION.
      ******************************************************************

      ******************************************************************
       DATA DIVISION.
      ******************************************************************
       WORKING-STORAGE SECTION.

      *----------------------------------------------------------------*
      * Common copybooks                                                *
      *----------------------------------------------------------------*
           COPY COCOM01Y.
           COPY COTTL01Y.
           COPY CSDAT01Y.
           COPY CSMSG01Y.
           COPY CSUSR01Y.
           COPY COADM1AI.
           COPY DFHAID.
           COPY DFHBMSCA.

      *----------------------------------------------------------------*
      * Working storage variables — all initialized per rule 8034      *
      *----------------------------------------------------------------*
       01  WS-MISC-STORAGE.
           05  WS-PGMNAME              PIC X(08) VALUE 'COADM01C'.
           05  WS-TRANID               PIC X(04) VALUE 'CA00'.
           05  WS-OPTION-SELECTED      PIC 9(02) VALUE ZEROS.
           05  WS-ERR-FLG              PIC X(01) VALUE SPACES.
               88  WS-ERR-FLG-ON                 VALUE 'Y'.
               88  WS-ERR-FLG-OFF                VALUE 'N'.
           05  WS-RESP                 PIC S9(08) COMP VALUE ZEROS.
           05  WS-RESP2                PIC S9(08) COMP VALUE ZEROS.
           05  WS-COMMAREA-LEN         PIC S9(04) COMP VALUE +0.

      *----------------------------------------------------------------*
      * Menu option constants                                           *
      *----------------------------------------------------------------*
       01  WS-MENU-OPTIONS.
           05  WS-OPT-USER-MGMT        PIC 9(02) VALUE 01.
           05  WS-OPT-ACCT-VIEW        PIC 9(02) VALUE 02.
           05  WS-OPT-ACCT-UPD         PIC 9(02) VALUE 03.
           05  WS-OPT-CARD-LIST        PIC 9(02) VALUE 04.
           05  WS-OPT-CARD-VIEW        PIC 9(02) VALUE 05.
           05  WS-OPT-CARD-UPD         PIC 9(02) VALUE 06.
           05  WS-OPT-TRANS-LIST       PIC 9(02) VALUE 07.
           05  WS-OPT-REPORTS          PIC 9(02) VALUE 08.
      *    --- TASK-043: new dashboard option appended as option 9 ---
           05  WS-OPT-DASHBOARD        PIC 9(02) VALUE 09.

      *----------------------------------------------------------------*
      * Screen title / header fields                                    *
      *----------------------------------------------------------------*
       01  WS-SCREEN-TITLE.
           05  WS-TITLE-LINE1          PIC X(40)
               VALUE 'CARDEMO ADMIN MENU                      '.
           05  WS-TITLE-LINE2          PIC X(40)
               VALUE 'PLEASE SELECT AN OPTION                 '.

      *----------------------------------------------------------------*
      * Error / info messages                                           *
      *----------------------------------------------------------------*
       01  WS-MESSAGES.
           05  WS-MSG-INVALID-OPT      PIC X(79)
               VALUE 'INVALID OPTION SELECTED. PLEASE TRY AGAIN.'.
           05  WS-MSG-XCTL-FAIL        PIC X(79)
               VALUE 'TRANSFER FAILED. PLEASE CONTACT SUPPORT.'.
           05  WS-MSG-SPACES           PIC X(79) VALUE SPACES.

      ******************************************************************
       LINKAGE SECTION.
       01  DFHCOMMAREA                 PIC X(32767).

      ******************************************************************
       PROCEDURE DIVISION.
      ******************************************************************

      *----------------------------------------------------------------*
      * 0000-MAIN                                                       *
      *   Entry point.  Validate session, dispatch on AID key.         *
      *----------------------------------------------------------------*
       0000-MAIN.

           MOVE EIBCALEN TO WS-COMMAREA-LEN

      *    --- Session / COMMAREA validation ---
           IF EIBCALEN = ZEROS
               PERFORM RETURN-TO-SIGNON-SCREEN
           END-IF

           MOVE DFHCOMMAREA TO WS-COMMAREA

           IF WS-COMMAREA-USER-ID = SPACES
           OR WS-COMMAREA-USER-ID = LOW-VALUES
               PERFORM RETURN-TO-SIGNON-SCREEN
           END-IF

      *    --- Clear error flag ---
           SET WS-ERR-FLG-OFF TO TRUE

      *    --- Dispatch on AID key ---
           EVALUATE TRUE
               WHEN EIBAID = DFHPF3
                   PERFORM RETURN-TO-SIGNON-SCREEN
               WHEN EIBAID = DFHPF12
                   PERFORM RETURN-TO-SIGNON-SCREEN
               WHEN EIBAID = DFHENTER
                   PERFORM PROCESS-ENTER-KEY
               WHEN OTHER
                   PERFORM 1000-SEND-MAP
           END-EVALUATE

           PERFORM COMMON-RETURN

           STOP RUN.

      *----------------------------------------------------------------*
      * 1000-SEND-MAP                                                   *
      *   Build and send the admin menu map to the terminal.           *
      *----------------------------------------------------------------*
       1000-SEND-MAP.

           PERFORM BUILD-MENU-OPTIONS

           EXEC CICS SEND
               MAP     ('COADM1A')
               MAPSET  ('COADM1A')
               FROM    (COADM1AO)
               ERASE
               RESP    (WS-RESP)
               RESP2   (WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

      *----------------------------------------------------------------*
      * BUILD-MENU-OPTIONS (paragraph ID: 13453)                       *
      *   Populate the map output area with menu option text.          *
      *   TASK-042: option 9 "View Account Dashboard" appended.        *
      *   All existing options (1-8) are unchanged.                    *
      *----------------------------------------------------------------*
       BUILD-MENU-OPTIONS.

           MOVE SPACES TO COADM1AO

      *    --- Header ---
           MOVE WS-TITLE-LINE1         TO COADM1ATITL1O
           MOVE WS-TITLE-LINE2         TO COADM1ATITL2O

      *    --- Option 1: User Management ---
           MOVE '1. USER MANAGEMENT'   TO COADM1AOPT1O

      *    --- Option 2: Account View ---
           MOVE '2. ACCOUNT VIEW'      TO COADM1AOPT2O

      *    --- Option 3: Account Update ---
           MOVE '3. ACCOUNT UPDATE'    TO COADM1AOPT3O

      *    --- Option 4: Card List ---
           MOVE '4. CARD LIST'         TO COADM1AOPT4O

      *    --- Option 5: Card View ---
           MOVE '5. CARD VIEW'         TO COADM1AOPT5O

      *    --- Option 6: Card Update ---
           MOVE '6. CARD UPDATE'       TO COADM1AOPT6O

      *    --- Option 7: Transaction List ---
           MOVE '7. TRANSACTION LIST'  TO COADM1AOPT7O

      *    --- Option 8: Reports ---
           MOVE '8. REPORTS'           TO COADM1AOPT8O

      *    --- Option 9: View Account Dashboard (TASK-042) ---
           MOVE '9. VIEW ACCOUNT DASHBOARD'
                                       TO COADM1AOPT9O

      *    --- PF key legend ---
           MOVE 'PF3=SIGN OFF  PF12=SIGN OFF'
                                       TO COADM1APFKYO

           IF WS-ERR-FLG-ON
               MOVE WS-MSG-INVALID-OPT TO COADM1AERRMSO
           ELSE
               MOVE WS-MSG-SPACES      TO COADM1AERRMSO
           END-IF.

      *----------------------------------------------------------------*
      * PROCESS-ENTER-KEY (paragraph ID: 14096)                        *
      *   Receive the map, validate the selected option, and XCTL      *
      *   to the appropriate program.                                   *
      *                                                                 *
      *   TASK-043: Added WHEN WS-OPT-DASHBOARD clause to XCTL to     *
      *             CODASH00C.  All existing WHEN clauses are          *
      *             unchanged.                                          *
      *----------------------------------------------------------------*
       PROCESS-ENTER-KEY.

      *    --- Receive the map from the terminal ---
           EXEC CICS RECEIVE
               MAP     ('COADM1A')
               MAPSET  ('COADM1A')
               INTO    (COADM1AI)
               RESP    (WS-RESP)
               RESP2   (WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF

      *    --- Convert entered option to numeric ---
           IF COADM1AOPTNI IS NUMERIC
               MOVE COADM1AOPTNI TO WS-OPTION-SELECTED
           ELSE
               MOVE ZEROS TO WS-OPTION-SELECTED
           END-IF

      *    --- Dispatch on selected option ---
           EVALUATE WS-OPTION-SELECTED

      *        --- Option 1: User Management ---
               WHEN WS-OPT-USER-MGMT
                   EXEC CICS XCTL
                       PROGRAM ('COUSR00C')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 2: Account View ---
               WHEN WS-OPT-ACCT-VIEW
                   EXEC CICS XCTL
                       PROGRAM ('COACTVWC')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 3: Account Update ---
               WHEN WS-OPT-ACCT-UPD
                   EXEC CICS XCTL
                       PROGRAM ('COACTUPC')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 4: Card List ---
               WHEN WS-OPT-CARD-LIST
                   EXEC CICS XCTL
                       PROGRAM ('COCRDSLC')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 5: Card View ---
               WHEN WS-OPT-CARD-VIEW
                   EXEC CICS XCTL
                       PROGRAM ('COCRDLIC')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 6: Card Update ---
               WHEN WS-OPT-CARD-UPD
                   EXEC CICS XCTL
                       PROGRAM ('COCRDUPC')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 7: Transaction List ---
               WHEN WS-OPT-TRANS-LIST
                   EXEC CICS XCTL
                       PROGRAM ('COTRN00C')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 8: Reports ---
               WHEN WS-OPT-REPORTS
                   EXEC CICS XCTL
                       PROGRAM ('CORPT00C')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Option 9: View Account Dashboard (TASK-043) ---
      *            Transfer control to the consolidated account tiles
      *            dashboard program.  RESP is checked immediately
      *            after the XCTL call per ISO-5055 rule 8162.
               WHEN WS-OPT-DASHBOARD
                   EXEC CICS XCTL
                       PROGRAM ('CODASH00C')
                       COMMAREA(WS-COMMAREA)
                       LENGTH  (WS-COMMAREA-LEN)
                       RESP    (WS-RESP)
                       RESP2   (WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
      *                XCTL failed — redisplay menu with error message
                       MOVE WS-MSG-XCTL-FAIL TO COADM1AERRMSO
                       PERFORM 1000-SEND-MAP
                   END-IF

      *        --- Invalid / unrecognised option ---
               WHEN OTHER
                   SET WS-ERR-FLG-ON TO TRUE
                   PERFORM 1000-SEND-MAP

           END-EVALUATE.

      *----------------------------------------------------------------*
      * COMMON-RETURN                                                   *
      *   Return to CICS with COMMAREA so state is preserved across    *
      *   pseudo-conversational interactions.                           *
      *----------------------------------------------------------------*
       COMMON-RETURN.

           EXEC CICS RETURN
               TRANSID (WS-TRANID)
               COMMAREA(WS-COMMAREA)
               LENGTH  (WS-COMMAREA-LEN)
               RESP    (WS-RESP)
               RESP2   (WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

      *----------------------------------------------------------------*
      * RETURN-TO-SIGNON-SCREEN (paragraph ID: 13890)                  *
      *   Transfer control back to the sign-on program when the        *
      *   session is absent, expired, or the user requests sign-off.   *
      *----------------------------------------------------------------*
       RETURN-TO-SIGNON-SCREEN.

           EXEC CICS XCTL
               PROGRAM ('COSGN00C')
               RESP    (WS-RESP)
               RESP2   (WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

      *----------------------------------------------------------------*
      * ABEND-ROUTINE                                                   *
      *   Issue a named CICS abend for diagnostic purposes.            *
      *----------------------------------------------------------------*
       ABEND-ROUTINE.

           EXEC CICS ABEND
               ABCODE ('ADM1')
               NODUMP
           END-EXEC.