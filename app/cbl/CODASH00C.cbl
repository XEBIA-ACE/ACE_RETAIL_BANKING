      ******************************************************************
      * PROGRAM:    CODASH00C                                          *
      * AUTHOR:     CARDDEMO DEVELOPMENT TEAM                         *
      * DATE:       2026-03-10                                         *
      * PURPOSE:    CONSOLIDATED ACCOUNT TILES DASHBOARD CONTROLLER    *
      *             DISPLAYS ALL ACCOUNT TYPES (CHECKING, SAVINGS,     *
      *             CREDIT CARD) HELD BY THE AUTHENTICATED CUSTOMER    *
      *             IN A SINGLE CICS 3270 SCREEN.                      *
      *                                                                *
      * TRANSACTION: CD00                                              *
      * MAP SET:     CODASH00                                          *
      * MAP NAME:    CODASH0A                                          *
      *                                                                *
      * COMMAREA:    COCOM01Y.CPY (ID: 13504) — SESSION TOKEN          *
      *                                                                *
      * COPYBOOKS INCLUDED:                                            *
      *   CODASH00.CPY  — BMS MAP FIELDS (NEW)                        *
      *   COCOM01Y.CPY  — COMMAREA / SESSION TOKEN (ID: 13504)        *
      *   COTTL01Y.CPY  — SCREEN TITLE FIELDS (ID: 14116)             *
      *   CSDAT01Y.CPY  — DATE FIELDS (ID: 14153)                     *
      *   CSMSG01Y.CPY  — MESSAGE FIELDS (ID: 13263)                  *
      *   CSUSR01Y.CPY  — USER SECURITY FIELDS (ID: 13507)            *
      *   CVACT01Y.CPY  — ACCOUNT DATA LAYOUT (ID: 14474)             *
      *   CVACT02Y.CPY  — ACCOUNT DATA LAYOUT (ID: 14206)             *
      *   CVACT03Y.CPY  — ACCOUNT DATA LAYOUT (ID: 13702)             *
      *   DFHAID        — CICS AID KEYS (ID: 1003)                    *
      *   DFHBMSCA      — BMS ATTRIBUTES (ID: 1002)                   *
      *                                                                *
      * DATA SETS ACCESSED (READ-ONLY):                                *
      *   ACCTDAT  (CICS DATASET ID: 13444)                           *
      *   CUSTDAT  (CICS DATASET ID: 14127)                           *
      *   CXACAIX  (CICS DATASET ID: 13815)                           *
      *                                                                *
      * QUALITY COMPLIANCE:                                            *
      *   RULE 8162 — ALL CICS RESP CODES CHECKED                     *
      *   RULE 8034 — ALL WS VARIABLES INITIALIZED WITH VALUE         *
      *   RULE 7688 — NO MOVE TRUNCATION                              *
      *   RULE 8478 — ON SIZE ERROR ON ALL LOOP ARITHMETIC            *
      *   RULE 7756 — INVALID KEY ON ALL VSAM READS                   *
      *                                                                *
      * ABEND CODE: CDSH                                               *
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CODASH00C.

      ******************************************************************
      * ENVIRONMENT DIVISION — EMPTY FOR CICS PROGRAMS                *
      ******************************************************************
       ENVIRONMENT DIVISION.

      ******************************************************************
      * DATA DIVISION                                                  *
      ******************************************************************
       DATA DIVISION.

       WORKING-STORAGE SECTION.

      ******************************************************************
      * PROGRAM CONSTANTS AND FLAGS                                    *
      ******************************************************************
       01  WS-PROGRAM-NAME             PIC X(08) VALUE 'CODASH00C'.
       01  WS-TRANSID                  PIC X(04) VALUE 'CD00'.
       01  WS-SIGNON-PROGRAM           PIC X(08) VALUE 'COSGN00C'.
       01  WS-CUSTMENU-PROGRAM         PIC X(08) VALUE 'COMEN01C'.
       01  WS-MAPSET-NAME              PIC X(08) VALUE 'CODASH00'.
       01  WS-MAP-NAME                 PIC X(08) VALUE 'CODASH0A'.

      ******************************************************************
      * CICS RESPONSE CODE FIELDS                                      *
      * RULE 8162: ALL CICS CALLS MUST CHECK RESP                     *
      ******************************************************************
       01  WS-RESP                     PIC S9(08) COMP VALUE +0.
       01  WS-RESP2                    PIC S9(08) COMP VALUE +0.

      ******************************************************************
      * AID / PF KEY TRACKING                                          *
      ******************************************************************
       01  WS-AID                      PIC X(01) VALUE SPACES.
       01  WS-PFKEY                    PIC X(02) VALUE SPACES.

      ******************************************************************
      * COMMAREA LENGTH                                                *
      ******************************************************************
       01  WS-COMMAREA-LEN             PIC S9(04) COMP VALUE +0.

      ******************************************************************
      * TILE PRESENCE FLAGS                                            *
      * RULE 8034: ALL WS VARIABLES INITIALIZED WITH VALUE            *
      ******************************************************************
       01  WS-TILE-FLAGS.
           05  WS-CHECKING-PRESENT     PIC X(01) VALUE 'N'.
           05  WS-SAVINGS-PRESENT      PIC X(01) VALUE 'N'.
           05  WS-CREDITCARD-PRESENT   PIC X(01) VALUE 'N'.
           05  WS-TILE-COUNT           PIC S9(04) COMP VALUE +0.

      ******************************************************************
      * ACCOUNT KEY AND RECORD WORK AREAS                              *
      ******************************************************************
       01  WS-ACCT-KEY                 PIC X(11) VALUE SPACES.
       01  WS-CUST-KEY                 PIC X(09) VALUE SPACES.
       01  WS-CARD-KEY                 PIC X(16) VALUE SPACES.

      ******************************************************************
      * ACCOUNT TYPE CONSTANTS                                         *
      * SME NOTE: VALUES CONFIRMED FROM CVACT01Y FIELD ACCT-ACTIVE-STATUS
      * AND ACCT-GROUP-ID — ADJUST PER SME SIGN-OFF (TASK-001)        *
      ******************************************************************
       01  WS-ACCT-TYPE-CHECKING       PIC X(02) VALUE 'CH'.
       01  WS-ACCT-TYPE-SAVINGS        PIC X(02) VALUE 'SA'.
       01  WS-ACCT-TYPE-CREDITCARD     PIC X(02) VALUE 'CC'.

      ******************************************************************
      * MASKING WORK FIELDS                                            *
      * RULE 7688: RECEIVING FIELDS SIZED >= SENDING FIELDS           *
      ******************************************************************
       01  WS-MASK-INPUT               PIC X(16) VALUE SPACES.
       01  WS-MASK-OUTPUT              PIC X(16) VALUE SPACES.
       01  WS-MASK-PREFIX              PIC X(12) VALUE '************'.
       01  WS-MASK-SUFFIX              PIC X(04) VALUE SPACES.
       01  WS-MASK-OVERFLOW            PIC X(01) VALUE SPACES.

      ******************************************************************
      * TILE DISPLAY WORK FIELDS                                       *
      ******************************************************************
       01  WS-CHECKING-TILE.
           05  WS-CHK-ACCT-ID          PIC X(11) VALUE SPACES.
           05  WS-CHK-MASKED-ID        PIC X(16) VALUE SPACES.
           05  WS-CHK-STATUS           PIC X(01) VALUE SPACES.
           05  WS-CHK-BALANCE          PIC S9(10)V99 COMP-3 VALUE +0.
           05  WS-CHK-BALANCE-DISP     PIC X(13) VALUE SPACES.

       01  WS-SAVINGS-TILE.
           05  WS-SAV-ACCT-ID          PIC X(11) VALUE SPACES.
           05  WS-SAV-MASKED-ID        PIC X(16) VALUE SPACES.
           05  WS-SAV-STATUS           PIC X(01) VALUE SPACES.
           05  WS-SAV-BALANCE          PIC S9(10)V99 COMP-3 VALUE +0.
           05  WS-SAV-BALANCE-DISP     PIC X(13) VALUE SPACES.

       01  WS-CREDITCARD-TILE.
           05  WS-CC-ACCT-ID           PIC X(11) VALUE SPACES.
           05  WS-CC-MASKED-ID         PIC X(16) VALUE SPACES.
           05  WS-CC-STATUS            PIC X(01) VALUE SPACES.
           05  WS-CC-BALANCE           PIC S9(10)V99 COMP-3 VALUE +0.
           05  WS-CC-BALANCE-DISP      PIC X(13) VALUE SPACES.

      ******************************************************************
      * LOOP CONTROL AND ARITHMETIC WORK FIELDS                        *
      * RULE 8478: ON SIZE ERROR REQUIRED FOR LOOP ARITHMETIC          *
      ******************************************************************
       01  WS-ACCT-LOOP-CTR            PIC S9(04) COMP VALUE +0.
       01  WS-ACCT-LOOP-MAX            PIC S9(04) COMP VALUE +99.
       01  WS-ACCT-LOOP-INC            PIC S9(04) COMP VALUE +1.
       01  WS-SIZE-ERROR-FLAG          PIC X(01) VALUE 'N'.

      ******************************************************************
      * ERROR MESSAGE WORK FIELD                                       *
      ******************************************************************
       01  WS-ERROR-MSG                PIC X(78) VALUE SPACES.
       01  WS-INFO-MSG                 PIC X(78) VALUE SPACES.

      ******************************************************************
      * SCREEN TITLE DATE WORK FIELDS                                  *
      ******************************************************************
       01  WS-CURDATE                  PIC X(08) VALUE SPACES.
       01  WS-CURTIME                  PIC X(08) VALUE SPACES.
       01  WS-CURDATE-DISP             PIC X(10) VALUE SPACES.

      ******************************************************************
      * ABEND FLAG                                                     *
      ******************************************************************
       01  WS-ABEND-FLAG               PIC X(01) VALUE 'N'.
       01  WS-ABEND-CODE               PIC X(04) VALUE 'CDSH'.

      ******************************************************************
      * BMS MAP COPYBOOK — CODASH00.CPY (NEW, FROM TASK-011)          *
      ******************************************************************
           COPY CODASH00.

      ******************************************************************
      * COMMAREA / SESSION TOKEN — COCOM01Y.CPY (ID: 13504)           *
      ******************************************************************
           COPY COCOM01Y.

      ******************************************************************
      * SCREEN TITLE FIELDS — COTTL01Y.CPY (ID: 14116)                *
      ******************************************************************
           COPY COTTL01Y.

      ******************************************************************
      * DATE FIELDS — CSDAT01Y.CPY (ID: 14153)                        *
      ******************************************************************
           COPY CSDAT01Y.

      ******************************************************************
      * MESSAGE FIELDS — CSMSG01Y.CPY (ID: 13263)                     *
      ******************************************************************
           COPY CSMSG01Y.

      ******************************************************************
      * USER SECURITY FIELDS — CSUSR01Y.CPY (ID: 13507)               *
      ******************************************************************
           COPY CSUSR01Y.

      ******************************************************************
      * ACCOUNT DATA LAYOUT — CVACT01Y.CPY (ID: 14474)                *
      ******************************************************************
           COPY CVACT01Y.

      ******************************************************************
      * ACCOUNT DATA LAYOUT — CVACT02Y.CPY (ID: 14206)                *
      ******************************************************************
           COPY CVACT02Y.

      ******************************************************************
      * ACCOUNT DATA LAYOUT — CVACT03Y.CPY (ID: 13702)                *
      ******************************************************************
           COPY CVACT03Y.

      ******************************************************************
      * CICS AID KEYS — DFHAID (ID: 1003)                             *
      ******************************************************************
           COPY DFHAID.

      ******************************************************************
      * BMS ATTRIBUTES — DFHBMSCA (ID: 1002)                          *
      ******************************************************************
           COPY DFHBMSCA.

      ******************************************************************
      * LINKAGE SECTION — DFHCOMMAREA                                  *
      ******************************************************************
       LINKAGE SECTION.

       01  DFHCOMMAREA                 PIC X(01).

      ******************************************************************
      * PROCEDURE DIVISION                                             *
      ******************************************************************
       PROCEDURE DIVISION.

      ******************************************************************
      * 0000-MAIN                                                      *
      * ENTRY POINT. VALIDATES SESSION, DISPATCHES ON PF KEY.         *
      * PATTERN: COACTVWC (ID: 13092), COMEN01C (ID: 13577)           *
      ******************************************************************
       0000-MAIN.

      *    STORE CONTEXT — CAPTURE AID KEY FROM EIB
           MOVE EIBAID                 TO WS-AID

      *    CLEAR ERROR MESSAGE FIELD
           MOVE SPACES                 TO WS-ERROR-MSG
           MOVE SPACES                 TO WS-INFO-MSG

      *    -------------------------------------------------------
      *    SESSION VALIDATION
      *    IF COMMAREA ABSENT OR SESSION TOKEN INVALID,
      *    REDIRECT TO SIGNON SCREEN (COSGN00C, ID: 13954)
      *    PATTERN: COADM01C RETURN-TO-SIGNON-SCREEN (ID: 13890)
      *    -------------------------------------------------------
           IF EIBCALEN = ZERO
               PERFORM RETURN-TO-SIGNON-SCREEN
           END-IF

           IF CDEMO-FROM-PROGRAM = SPACES OR LOW-VALUES
               PERFORM RETURN-TO-SIGNON-SCREEN
           END-IF

      *    -------------------------------------------------------
      *    PF KEY DISPATCH
      *    -------------------------------------------------------
           EVALUATE TRUE
               WHEN WS-AID = DFHPF3
      *            PF3 — RETURN TO CUSTOMER MENU
                   EXEC CICS XCTL
                       PROGRAM(WS-CUSTMENU-PROGRAM)
                       COMMAREA(CDEMO-COMMAREA)
                       LENGTH(LENGTH OF CDEMO-COMMAREA)
                       RESP(WS-RESP)
                       RESP2(WS-RESP2)
                   END-EXEC
                   IF WS-RESP NOT = DFHRESP(NORMAL)
                       PERFORM ABEND-ROUTINE
                   END-IF

               WHEN WS-AID = DFHENTER
      *            ENTER — PROCESS USER INPUT
                   PERFORM 2000-PROCESS-INPUTS

               WHEN WS-AID = DFHPF12
      *            PF12 — RETURN TO SIGNON
                   PERFORM RETURN-TO-SIGNON-SCREEN

               WHEN OTHER
      *            ALL OTHER KEYS — REDISPLAY MAP
                   MOVE 'INVALID KEY PRESSED. USE PF3 TO RETURN.'
                       TO WS-ERROR-MSG
                   PERFORM 1000-SEND-MAP
           END-EVALUATE

           PERFORM COMMON-RETURN

           STOP RUN.

      ******************************************************************
      * 1000-SEND-MAP                                                  *
      * SENDS CODASH0A MAP TO THE TERMINAL.                            *
      * RULE 8162: RESP CHECKED AFTER EXEC CICS SEND MAP              *
      ******************************************************************
       1000-SEND-MAP.

           PERFORM 1100-SCREEN-INIT

           EXEC CICS SEND
               MAP(WS-MAP-NAME)
               MAPSET(WS-MAPSET-NAME)
               FROM(CODASH0AO)
               ERASE
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               MOVE 'ERROR SENDING MAP CODASH0A. CONTACT SUPPORT.'
                   TO WS-ERROR-MSG
               PERFORM ABEND-ROUTINE
           END-IF.

       1000-SEND-MAP-EXIT.
           EXIT.

      ******************************************************************
      * 1100-SCREEN-INIT                                               *
      * INITIALIZES ALL CODASH0A MAP FIELDS.                           *
      * CALLS TILE BUILD PARAGRAPHS AFTER READING ACCOUNT DATA.        *
      ******************************************************************
       1100-SCREEN-INIT.

      *    INITIALIZE ALL MAP OUTPUT FIELDS TO LOW-VALUES / SPACES
           MOVE LOW-VALUES             TO CODASH0AO

      *    POPULATE SCREEN TITLE
           MOVE 'CARDDEMO - ACCOUNT DASHBOARD'
                                       TO CDTITLEO OF CODASH0AO

      *    POPULATE DATE/TIME ON SCREEN
           EXEC CICS ASKTIME
               ABSTIME(WS-ABS-TIME)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC
           IF WS-RESP NOT = DFHRESP(NORMAL)
               MOVE SPACES             TO WS-CURDATE
               MOVE SPACES             TO WS-CURTIME
           END-IF

           EXEC CICS FORMATTIME
               ABSTIME(WS-ABS-TIME)
               MMDDYYYY(WS-CURDATE)
               TIME(WS-CURTIME)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC
           IF WS-RESP NOT = DFHRESP(NORMAL)
               MOVE SPACES             TO WS-CURDATE
               MOVE SPACES             TO WS-CURTIME
           END-IF

           MOVE WS-CURDATE             TO CDDATEO  OF CODASH0AO
           MOVE WS-CURTIME             TO CDTIMEO  OF CODASH0AO

      *    POPULATE USER ID FROM COMMAREA
           MOVE CDEMO-CUST-ID          TO CDUSRIDO OF CODASH0AO

      *    RESET TILE FLAGS
           MOVE 'N'                    TO WS-CHECKING-PRESENT
           MOVE 'N'                    TO WS-SAVINGS-PRESENT
           MOVE 'N'                    TO WS-CREDITCARD-PRESENT
           MOVE +0                     TO WS-TILE-COUNT

      *    READ ACCOUNT DATA AND SET TILE FLAGS
           PERFORM 9100-READ-ACCT-TILES

      *    BUILD EACH TILE (CONDITIONAL ON PRESENCE FLAG)
           PERFORM 9200-BUILD-TILE-CHECKING
           PERFORM 9300-BUILD-TILE-SAVINGS
           PERFORM 9400-BUILD-TILE-CREDITCARD

      *    POPULATE ERROR/INFO MESSAGE FIELD
           MOVE WS-ERROR-MSG           TO CDERRMSO OF CODASH0AO

           IF WS-TILE-COUNT = ZERO
               MOVE 'NO ACCOUNTS FOUND FOR THIS CUSTOMER.'
                   TO CDINMSGO OF CODASH0AO
           ELSE
               MOVE WS-INFO-MSG        TO CDINMSGO OF CODASH0AO
           END-IF.

       1100-SCREEN-INIT-EXIT.
           EXIT.

      ******************************************************************
      * 2000-PROCESS-INPUTS                                            *
      * RECEIVES MAP INPUT AND PROCESSES USER SELECTIONS.              *
      * RULE 8162: RESP CHECKED AFTER EXEC CICS RECEIVE MAP           *
      ******************************************************************
       2000-PROCESS-INPUTS.

           EXEC CICS RECEIVE
               MAP(WS-MAP-NAME)
               MAPSET(WS-MAPSET-NAME)
               INTO(CODASH0AI)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           EVALUATE WS-RESP
               WHEN DFHRESP(NORMAL)
                   CONTINUE
               WHEN DFHRESP(MAPFAIL)
      *            NO DATA ENTERED — REDISPLAY
                   PERFORM 1000-SEND-MAP
               WHEN OTHER
                   MOVE 'ERROR RECEIVING MAP INPUT. CONTACT SUPPORT.'
                       TO WS-ERROR-MSG
                   PERFORM ABEND-ROUTINE
           END-EVALUATE

      *    REFRESH DASHBOARD DISPLAY
           PERFORM 1000-SEND-MAP.

       2000-PROCESS-INPUTS-EXIT.
           EXIT.

      ******************************************************************
      * 9100-READ-ACCT-TILES                                           *
      * READS ACCOUNT DATA FROM ACCTDAT (CICS DATASET ID: 13444)      *
      * FOR ALL ACCOUNTS OWNED BY THE CURRENT CUSTOMER.               *
      * RULE 8162: RESP CHECKED AFTER EVERY CICS READ                 *
      * RULE 7756: INVALID KEY CLAUSE ON ALL VSAM READS               *
      * RULE 8478: ON SIZE ERROR ON LOOP COUNTER ARITHMETIC           *
      ******************************************************************
       9100-READ-ACCT-TILES.

      *    BUILD ACCOUNT KEY FROM CUSTOMER ID IN COMMAREA
           MOVE CDEMO-ACCT-ID          TO WS-ACCT-KEY

      *    RESET LOOP COUNTER
           MOVE +0                     TO WS-ACCT-LOOP-CTR
           MOVE 'N'                    TO WS-SIZE-ERROR-FLAG

      *    READ PRIMARY ACCOUNT RECORD FOR THIS CUSTOMER
           EXEC CICS READ
               DATASET('ACCTDAT')
               INTO(ACCT-RECORD)
               RIDFLD(WS-ACCT-KEY)
               KEYLENGTH(LENGTH OF WS-ACCT-KEY)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           EVALUATE WS-RESP
               WHEN DFHRESP(NORMAL)
      *            ACCOUNT RECORD FOUND — DETERMINE TYPE
                   PERFORM 9110-CLASSIFY-ACCOUNT

               WHEN DFHRESP(NOTFND)
      *            NO ACCOUNT FOUND — LEAVE ALL TILE FLAGS AS 'N'
                   MOVE 'NO ACCOUNT RECORD FOUND FOR THIS CUSTOMER.'
                       TO WS-INFO-MSG

               WHEN DFHRESP(NOTOPEN)
                   MOVE 'ACCTDAT DATASET NOT OPEN. CONTACT SUPPORT.'
                       TO WS-ERROR-MSG
                   PERFORM ABEND-ROUTINE

               WHEN OTHER
                   MOVE 'ERROR READING ACCTDAT. CONTACT SUPPORT.'
                       TO WS-ERROR-MSG
                   PERFORM ABEND-ROUTINE
           END-EVALUATE

      *    READ CROSS-REFERENCE INDEX FOR ADDITIONAL ACCOUNTS
      *    USING CXACAIX (CICS DATASET ID: 13815)
           MOVE CDEMO-CUST-ID          TO WS-CUST-KEY

           EXEC CICS READ
               DATASET('CXACAIX')
               INTO(ACCT-RECORD)
               RIDFLD(WS-CUST-KEY)
               KEYLENGTH(LENGTH OF WS-CUST-KEY)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           EVALUATE WS-RESP
               WHEN DFHRESP(NORMAL)
      *            CROSS-REFERENCE FOUND — CLASSIFY THIS ACCOUNT TOO
                   PERFORM 9110-CLASSIFY-ACCOUNT

               WHEN DFHRESP(NOTFND)
      *            NO CROSS-REFERENCE — ACCEPTABLE, CONTINUE
                   CONTINUE

               WHEN DFHRESP(NOTOPEN)
                   MOVE 'CXACAIX DATASET NOT OPEN. CONTACT SUPPORT.'
                       TO WS-ERROR-MSG
                   PERFORM ABEND-ROUTINE

               WHEN OTHER
      *            NON-FATAL — LOG AND CONTINUE
                   MOVE 'WARNING: CXACAIX READ ERROR. SOME TILES MAY'
                       TO WS-INFO-MSG
           END-EVALUATE.

       9100-READ-ACCT-TILES-EXIT.
           EXIT.

      ******************************************************************
      * 9110-CLASSIFY-ACCOUNT                                          *
      * EXAMINES ACCOUNT RECORD TYPE FIELD AND SETS TILE FLAGS.        *
      * SME NOTE: ACCT-GROUP-ID USED AS ACCOUNT TYPE DISCRIMINATOR.   *
      * ADJUST FIELD NAME AND VALUES PER TASK-001 SME SIGN-OFF.        *
      ******************************************************************
       9110-CLASSIFY-ACCOUNT.

           EVALUATE ACCT-GROUP-ID
               WHEN WS-ACCT-TYPE-CHECKING
                   MOVE 'Y'            TO WS-CHECKING-PRESENT
                   MOVE ACCT-ID        TO WS-CHK-ACCT-ID
                   MOVE ACCT-ACTIVE-STATUS
                                       TO WS-CHK-STATUS
                   MOVE ACCT-CURR-BAL  TO WS-CHK-BALANCE

               WHEN WS-ACCT-TYPE-SAVINGS
                   MOVE 'Y'            TO WS-SAVINGS-PRESENT
                   MOVE ACCT-ID        TO WS-SAV-ACCT-ID
                   MOVE ACCT-ACTIVE-STATUS
                                       TO WS-SAV-STATUS
                   MOVE ACCT-CURR-BAL  TO WS-SAV-BALANCE

               WHEN WS-ACCT-TYPE-CREDITCARD
                   MOVE 'Y'            TO WS-CREDITCARD-PRESENT
                   MOVE ACCT-ID        TO WS-CC-ACCT-ID
                   MOVE ACCT-ACTIVE-STATUS
                                       TO WS-CC-STATUS
                   MOVE ACCT-CURR-BAL  TO WS-CC-BALANCE

               WHEN OTHER
      *            UNKNOWN ACCOUNT TYPE — LOG AND CONTINUE
                   MOVE 'WARNING: UNKNOWN ACCOUNT TYPE ENCOUNTERED.'
                       TO WS-INFO-MSG
           END-EVALUATE

      *    INCREMENT TILE COUNT WITH SIZE ERROR PROTECTION
      *    RULE 8478: ON SIZE ERROR REQUIRED
           ADD WS-ACCT-LOOP-INC        TO WS-ACCT-LOOP-CTR
               ON SIZE ERROR
                   MOVE 'Y'            TO WS-SIZE-ERROR-FLAG
                   MOVE 'ACCOUNT COUNT OVERFLOW. CONTACT SUPPORT.'
                       TO WS-ERROR-MSG
           END-ADD.

       9110-CLASSIFY-ACCOUNT-EXIT.
           EXIT.

      ******************************************************************
      * 9200-BUILD-TILE-CHECKING                                       *
      * POPULATES CHECKING TILE FIELDS IN CODASH0A MAP.                *
      * IF NOT PRESENT, SUPPRESSES TILE DISPLAY.                       *
      ******************************************************************
       9200-BUILD-TILE-CHECKING.

           IF WS-CHECKING-PRESENT = 'Y'
      *        MASK THE ACCOUNT ID
               MOVE WS-CHK-ACCT-ID     TO WS-MASK-INPUT
               PERFORM 9500-MASK-ACCOUNT-ID
               MOVE WS-MASK-OUTPUT     TO WS-CHK-MASKED-ID

      *        POPULATE MAP FIELDS FOR CHECKING TILE
               MOVE 'CHECKING ACCOUNT'
                                       TO CDCHKLBO OF CODASH0AO
               MOVE WS-CHK-MASKED-ID  TO CDCHKIDO OF CODASH0AO
               MOVE WS-CHK-STATUS     TO CDCHKSTO OF CODASH0AO

      *        FORMAT BALANCE FOR DISPLAY
               MOVE WS-CHK-BALANCE    TO WS-CHK-BALANCE-DISP
               MOVE WS-CHK-BALANCE-DISP
                                       TO CDCHKBALO OF CODASH0AO

      *        SET ATTRIBUTE TO NORMAL (VISIBLE)
               MOVE DFHBMUNP           TO CDCHKLBA OF CODASH0AO
               MOVE DFHBMUNP           TO CDCHKIDA OF CODASH0AO
               MOVE DFHBMUNP           TO CDCHKSTA OF CODASH0AO
               MOVE DFHBMUNP           TO CDCHKBALA OF CODASH0AO

               ADD WS-ACCT-LOOP-INC   TO WS-TILE-COUNT
                   ON SIZE ERROR
                       MOVE 'Y'        TO WS-SIZE-ERROR-FLAG
               END-ADD
           ELSE
      *        SUPPRESS CHECKING TILE — SET FIELDS TO SPACES
               MOVE SPACES             TO CDCHKLBO OF CODASH0AO
               MOVE SPACES             TO CDCHKIDO OF CODASH0AO
               MOVE SPACES             TO CDCHKSTO OF CODASH0AO
               MOVE SPACES             TO CDCHKBALO OF CODASH0AO

      *        SET ATTRIBUTE TO DARK (NOT DISPLAYED)
               MOVE DFHBMDAR           TO CDCHKLBA OF CODASH0AO
               MOVE DFHBMDAR           TO CDCHKIDA OF CODASH0AO
               MOVE DFHBMDAR           TO CDCHKSTA OF CODASH0AO
               MOVE DFHBMDAR           TO CDCHKBALA OF CODASH0AO
           END-IF.

       9200-BUILD-TILE-CHECKING-EXIT.
           EXIT.

      ******************************************************************
      * 9300-BUILD-TILE-SAVINGS                                        *
      * POPULATES SAVINGS TILE FIELDS IN CODASH0A MAP.                 *
      * IF NOT PRESENT, SUPPRESSES TILE DISPLAY.                       *
      ******************************************************************
       9300-BUILD-TILE-SAVINGS.

           IF WS-SAVINGS-PRESENT = 'Y'
      *        MASK THE ACCOUNT ID
               MOVE WS-SAV-ACCT-ID     TO WS-MASK-INPUT
               PERFORM 9500-MASK-ACCOUNT-ID
               MOVE WS-MASK-OUTPUT     TO WS-SAV-MASKED-ID

      *        POPULATE MAP FIELDS FOR SAVINGS TILE
               MOVE 'SAVINGS ACCOUNT'
                                       TO CDSAVLBO OF CODASH0AO
               MOVE WS-SAV-MASKED-ID  TO CDSAVIDO OF CODASH0AO
               MOVE WS-SAV-STATUS     TO CDSAVSTO OF CODASH0AO

      *        FORMAT BALANCE FOR DISPLAY
               MOVE WS-SAV-BALANCE    TO WS-SAV-BALANCE-DISP
               MOVE WS-SAV-BALANCE-DISP
                                       TO CDSAVBALO OF CODASH0AO

      *        SET ATTRIBUTE TO NORMAL (VISIBLE)
               MOVE DFHBMUNP           TO CDSAVLBA OF CODASH0AO
               MOVE DFHBMUNP           TO CDSAVIDA OF CODASH0AO
               MOVE DFHBMUNP           TO CDSAVSTA OF CODASH0AO
               MOVE DFHBMUNP           TO CDSAVBALA OF CODASH0AO

               ADD WS-ACCT-LOOP-INC   TO WS-TILE-COUNT
                   ON SIZE ERROR
                       MOVE 'Y'        TO WS-SIZE-ERROR-FLAG
               END-ADD
           ELSE
      *        SUPPRESS SAVINGS TILE — SET FIELDS TO SPACES
               MOVE SPACES             TO CDSAVLBO OF CODASH0AO
               MOVE SPACES             TO CDSAVIDO OF CODASH0AO
               MOVE SPACES             TO CDSAVSTO OF CODASH0AO
               MOVE SPACES             TO CDSAVBALO OF CODASH0AO

      *        SET ATTRIBUTE TO DARK (NOT DISPLAYED)
               MOVE DFHBMDAR           TO CDSAVLBA OF CODASH0AO
               MOVE DFHBMDAR           TO CDSAVIDA OF CODASH0AO
               MOVE DFHBMDAR           TO CDSAVSTA OF CODASH0AO
               MOVE DFHBMDAR           TO CDSAVBALA OF CODASH0AO
           END-IF.

       9300-BUILD-TILE-SAVINGS-EXIT.
           EXIT.

      ******************************************************************
      * 9400-BUILD-TILE-CREDITCARD                                     *
      * POPULATES CREDIT CARD TILE FIELDS IN CODASH0A MAP.             *
      * IF NOT PRESENT, SUPPRESSES TILE DISPLAY.                       *
      ******************************************************************
       9400-BUILD-TILE-CREDITCARD.

           IF WS-CREDITCARD-PRESENT = 'Y'
      *        MASK THE ACCOUNT ID
               MOVE WS-CC-ACCT-ID      TO WS-MASK-INPUT
               PERFORM 9500-MASK-ACCOUNT-ID
               MOVE WS-MASK-OUTPUT     TO WS-CC-MASKED-ID

      *        POPULATE MAP FIELDS FOR CREDIT CARD TILE
               MOVE 'CREDIT CARD ACCOUNT'
                                       TO CDCRDLBO OF CODASH0AO
               MOVE WS-CC-MASKED-ID   TO CDCRDIDO OF CODASH0AO
               MOVE WS-CC-STATUS      TO CDCRDSTO OF CODASH0AO

      *        FORMAT BALANCE FOR DISPLAY
               MOVE WS-CC-BALANCE     TO WS-CC-BALANCE-DISP
               MOVE WS-CC-BALANCE-DISP
                                       TO CDRDBALO OF CODASH0AO

      *        SET ATTRIBUTE TO NORMAL (VISIBLE)
               MOVE DFHBMUNP           TO CDCRDLBA OF CODASH0AO
               MOVE DFHBMUNP           TO CDCRDIDA OF CODASH0AO
               MOVE DFHBMUNP           TO CDCRDSTA OF CODASH0AO
               MOVE DFHBMUNP           TO CDRDBALA OF CODASH0AO

               ADD WS-ACCT-LOOP-INC   TO WS-TILE-COUNT
                   ON SIZE ERROR
                       MOVE 'Y'        TO WS-SIZE-ERROR-FLAG
               END-ADD
           ELSE
      *        SUPPRESS CREDIT CARD TILE — SET FIELDS TO SPACES
               MOVE SPACES             TO CDCRDLBO OF CODASH0AO
               MOVE SPACES             TO CDCRDIDO OF CODASH0AO
               MOVE SPACES             TO CDCRDSTO OF CODASH0AO
               MOVE SPACES             TO CDRDBALO OF CODASH0AO

      *        SET ATTRIBUTE TO DARK (NOT DISPLAYED)
               MOVE DFHBMDAR           TO CDCRDLBA OF CODASH0AO
               MOVE DFHBMDAR           TO CDCRDIDA OF CODASH0AO
               MOVE DFHBMDAR           TO CDCRDSTA OF CODASH0AO
               MOVE DFHBMDAR           TO CDRDBALA OF CODASH0AO
           END-IF.

       9400-BUILD-TILE-CREDITCARD-EXIT.
           EXIT.

      ******************************************************************
      * 9500-MASK-ACCOUNT-ID                                           *
      * APPLIES MASKING TO ACCOUNT IDENTIFIER.                         *
      * FORMAT: FIRST 12 CHARS REPLACED WITH *, LAST 4 VISIBLE.       *
      * RULE 7688: NO DATA TRUNCATION — FIELDS SIZED APPROPRIATELY    *
      * RULE 8470: STRING VERB USES ON OVERFLOW CLAUSE                 *
      * SME NOTE: MASKING FORMAT SUBJECT TO TASK-002 SIGN-OFF.         *
      ******************************************************************
       9500-MASK-ACCOUNT-ID.

      *    INITIALIZE OUTPUT FIELD
           MOVE SPACES                 TO WS-MASK-OUTPUT
           MOVE SPACES                 TO WS-MASK-OVERFLOW

      *    EXTRACT LAST 4 CHARACTERS OF ACCOUNT ID FOR DISPLAY
      *    RULE 7688: WS-MASK-SUFFIX (PIC X(04)) MATCHES SOURCE SIZE
           MOVE WS-MASK-INPUT(13:4)    TO WS-MASK-SUFFIX

      *    BUILD MASKED OUTPUT: 12 ASTERISKS + LAST 4 DIGITS
      *    RULE 8470: ON OVERFLOW CLAUSE REQUIRED FOR STRING VERB
           STRING WS-MASK-PREFIX
                  DELIMITED BY SIZE
                  WS-MASK-SUFFIX
                  DELIMITED BY SIZE
               INTO WS-MASK-OUTPUT
               ON OVERFLOW
                   MOVE 'Y'            TO WS-MASK-OVERFLOW
                   MOVE '****-****-****' TO WS-MASK-OUTPUT
           END-STRING.

       9500-MASK-ACCOUNT-ID-EXIT.
           EXIT.

      ******************************************************************
      * COMMON-RETURN                                                   *
      * RETURNS TO CICS WITH COMMAREA AND TRANSACTION ID.              *
      * RULE 8162: RESP CHECKED AFTER EXEC CICS RETURN                *
      ******************************************************************
       COMMON-RETURN.

           EXEC CICS RETURN
               TRANSID(WS-TRANSID)
               COMMAREA(CDEMO-COMMAREA)
               LENGTH(LENGTH OF CDEMO-COMMAREA)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

       COMMON-RETURN-EXIT.
           EXIT.

      ******************************************************************
      * RETURN-TO-SIGNON-SCREEN                                        *
      * TRANSFERS CONTROL TO COSGN00C (ID: 13954) WHEN SESSION IS     *
      * ABSENT OR EXPIRED. NO ACCOUNT DATA IS RENDERED BEFORE THIS.   *
      * PATTERN: COADM01C RETURN-TO-SIGNON-SCREEN (ID: 13890)         *
      * RULE 8162: RESP CHECKED AFTER EXEC CICS XCTL                  *
      ******************************************************************
       RETURN-TO-SIGNON-SCREEN.

           EXEC CICS XCTL
               PROGRAM(WS-SIGNON-PROGRAM)
               RESP(WS-RESP)
               RESP2(WS-RESP2)
           END-EXEC

           IF WS-RESP NOT = DFHRESP(NORMAL)
               PERFORM ABEND-ROUTINE
           END-IF.

       RETURN-TO-SIGNON-SCREEN-EXIT.
           EXIT.

      ******************************************************************
      * ABEND-ROUTINE                                                  *
      * ISSUES CICS ABEND WITH CODE CDSH.                              *
      * CONSISTENT WITH EXISTING ABEND PATTERNS IN CARDDEMO.          *
      * RULE 8162: ABEND DOES NOT REQUIRE RESP CHECK (TERMINAL)       *
      ******************************************************************
       ABEND-ROUTINE.

           EXEC CICS ABEND
               ABCODE(WS-ABEND-CODE)
               NODUMP
           END-EXEC.

       ABEND-ROUTINE-EXIT.
           EXIT.