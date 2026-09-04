      ******************************************************************
      * COPYBOOK:  CODASH00.CPY                                        *
      * PURPOSE:   BMS SYMBOLIC MAP FOR CODASH0A                       *
      *            CONSOLIDATED ACCOUNT TILES DASHBOARD                *
      * GENERATED: FROM CODASH00.BMS (TASK-011)                        *
      * MAP SET:   CODASH00                                             *
      * MAP NAME:  CODASH0A                                             *
      * DATE:      2026-03-10                                           *
      *                                                                *
      * FIELD NAMING CONVENTION:                                        *
      *   INPUT  FIELDS: CODASH0AI (SUFFIX I)                          *
      *   OUTPUT FIELDS: CODASH0AO (SUFFIX O)                          *
      *   ATTRIBUTE BYTE: SUFFIX A                                      *
      *   LENGTH FIELD:   SUFFIX L                                      *
      ******************************************************************
      *
      * INPUT MAP STRUCTURE
      *
       01  CODASH0AI.
      *
      *    FILLER — TIOAPFX
           02  FILLER                  PIC X(12).
      *
      *    SCREEN TITLE
           02  CDTITLEL                PIC S9(04) COMP.
           02  CDTITLEA                PIC X(01).
           02  CDTITLEI                PIC X(38).
      *
      *    DATE FIELD
           02  CDDATEL                 PIC S9(04) COMP.
           02  CDDATEA                 PIC X(01).
           02  CDDATEI                 PIC X(10).
      *
      *    TIME FIELD
           02  CDTIMEL                 PIC S9(04) COMP.
           02  CDTIMEA                 PIC X(01).
           02  CDTIMEI                 PIC X(08).
      *
      *    USER ID FIELD
           02  CDUSRIDL                PIC S9(04) COMP.
           02  CDUSRIDA                PIC X(01).
           02  CDUSRIDI                PIC X(08).
      *
      *    CHECKING TILE — LABEL
           02  CDCHKLBL                PIC S9(04) COMP.
           02  CDCHKLBA                PIC X(01).
           02  CDCHKLBI                PIC X(20).
      *
      *    CHECKING TILE — ACCOUNT ID (MASKED)
           02  CDCHKIDL                PIC S9(04) COMP.
           02  CDCHKIDA                PIC X(01).
           02  CDCHKIDI                PIC X(16).
      *
      *    CHECKING TILE — STATUS
           02  CDCHKSTL                PIC S9(04) COMP.
           02  CDCHKSTA                PIC X(01).
           02  CDCHKSTI                PIC X(01).
      *
      *    CHECKING TILE — BALANCE
           02  CDCHKBALL               PIC S9(04) COMP.
           02  CDCHKBALA               PIC X(01).
           02  CDCHKBALI               PIC X(13).
      *
      *    SAVINGS TILE — LABEL
           02  CDSAVLBL                PIC S9(04) COMP.
           02  CDSAVLBA                PIC X(01).
           02  CDSAVLBI                PIC X(20).
      *
      *    SAVINGS TILE — ACCOUNT ID (MASKED)
           02  CDSAVIDL                PIC S9(04) COMP.
           02  CDSAVIDA                PIC X(01).
           02  CDSAVIDI                PIC X(16).
      *
      *    SAVINGS TILE — STATUS
           02  CDSAVSTL                PIC S9(04) COMP.
           02  CDSAVSTA                PIC X(01).
           02  CDSAVSTI                PIC X(01).
      *
      *    SAVINGS TILE — BALANCE
           02  CDSAVBALL               PIC S9(04) COMP.
           02  CDSAVBALA               PIC X(01).
           02  CDSAVBALI               PIC X(13).
      *
      *    CREDIT CARD TILE — LABEL
           02  CDCRDLBL                PIC S9(04) COMP.
           02  CDCRDLBA                PIC X(01).
           02  CDCRDLBI                PIC X(20).
      *
      *    CREDIT CARD TILE — ACCOUNT ID (MASKED)
           02  CDCRDIDL                PIC S9(04) COMP.
           02  CDCRDIDA                PIC X(01).
           02  CDCRDIDI                PIC X(16).
      *
      *    CREDIT CARD TILE — STATUS
           02  CDCRDSTL                PIC S9(04) COMP.
           02  CDCRDSTA                PIC X(01).
           02  CDCRDSTI                PIC X(01).
      *
      *    CREDIT CARD TILE — BALANCE
           02  CDRDBALL                PIC S9(04) COMP.
           02  CDRDBALA                PIC X(01).
           02  CDRDBALI                PIC X(13).
      *
      *    INFO MESSAGE
           02  CDINMSGL                PIC S9(04) COMP.
           02  CDINMSGA                PIC X(01).
           02  CDINMSGI                PIC X(50).
      *
      *    ERROR MESSAGE
           02  CDERMSL                 PIC S9(04) COMP.
           02  CDERMSA                 PIC X(01).
           02  CDERMSI                 PIC X(78).
      *
      ******************************************************************
      * OUTPUT MAP STRUCTURE — REDEFINES INPUT MAP                     *
      ******************************************************************
      *
       01  CODASH0AO REDEFINES CODASH0AI.
      *
      *    FILLER — TIOAPFX
           02  FILLER                  PIC X(12).
      *
      *    SCREEN TITLE
           02  FILLER                  PIC X(02).
           02  CDTITLEO                PIC X(38).
      *
      *    DATE FIELD
           02  FILLER                  PIC X(02).
           02  CDDATEO                 PIC X(10).
      *
      *    TIME FIELD
           02  FILLER                  PIC X(02).
           02  CDTIMEO                 PIC X(08).
      *
      *    USER ID FIELD
           02  FILLER                  PIC X(02).
           02  CDUSRIDO                PIC X(08).
      *
      *    CHECKING TILE — LABEL
           02  FILLER                  PIC X(02).
           02  CDCHKLBO                PIC X(20).
      *
      *    CHECKING TILE — ACCOUNT ID (MASKED)
           02  FILLER                  PIC X(02).
           02  CDCHKIDO                PIC X(16).
      *
      *    CHECKING TILE — STATUS
           02  FILLER                  PIC X(02).
           02  CDCHKSTO                PIC X(01).
      *
      *    CHECKING TILE — BALANCE
           02  FILLER                  PIC X(02).
           02  CDCHKBALO               PIC X(13).
      *
      *    SAVINGS TILE — LABEL
           02  FILLER                  PIC X(02).
           02  CDSAVLBO                PIC X(20).
      *
      *    SAVINGS TILE — ACCOUNT ID (MASKED)
           02  FILLER                  PIC X(02).
           02  CDSAVIDO                PIC X(16).
      *
      *    SAVINGS TILE — STATUS
           02  FILLER                  PIC X(02).
           02  CDSAVSTO                PIC X(01).
      *
      *    SAVINGS TILE — BALANCE
           02  FILLER                  PIC X(02).
           02  CDSAVBALO               PIC X(13).
      *
      *    CREDIT CARD TILE — LABEL
           02  FILLER                  PIC X(02).
           02  CDCRDLBO                PIC X(20).
      *
      *    CREDIT CARD TILE — ACCOUNT ID (MASKED)
           02  FILLER                  PIC X(02).
           02  CDCRDIDO                PIC X(16).
      *
      *    CREDIT CARD TILE — STATUS
           02  FILLER                  PIC X(02).
           02  CDCRDSTO                PIC X(01).
      *
      *    CREDIT CARD TILE — BALANCE
           02  FILLER                  PIC X(02).
           02  CDRDBALO                PIC X(13).
      *
      *    INFO MESSAGE
           02  FILLER                  PIC X(02).
           02  CDINMSGO                PIC X(50).
      *
      *    ERROR MESSAGE
           02  FILLER                  PIC X(02).
           02  CDERRMSO                PIC X(78).
      *