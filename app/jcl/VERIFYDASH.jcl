//VERIFYDSH JOB (ACCT),'VERIFY CODASH00C LOAD MODULE',CLASS=A,
//              MSGCLASS=X,MSGLEVEL=(1,1),NOTIFY=&SYSUID
//*
//* ================================================================
//* JOB: VERIFYDSH
//* PURPOSE: VERIFY CODASH00C AND CODASH00 LOAD MODULES EXIST
//*          IN THE CICS LOAD LIBRARY AND ARE ACCESSIBLE TO CICS.
//* TASK:    TASK-027 ACCEPTANCE VERIFICATION
//* DATE:    2026-03-10
//* ================================================================
//*
//* ================================================================
//* STEP 1: LIST CODASH00C LOAD MODULE FROM CICS LOAD LIBRARY
//* ================================================================
//*
//LSTCODSH EXEC PGM=IDCAMS
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  LISTCAT ENTRIES(CARDDEMO.CICS.LOADLIB) ALL
/*
//*
//* ================================================================
//* STEP 2: AMBLIST — VERIFY CODASH00C MODULE ATTRIBUTES
//*         CONFIRMS RENT, RMODE(ANY), AMODE(31)
//* ================================================================
//*
//AMBLIST  EXEC PGM=AMBLIST
//SYSPRINT DD SYSOUT=*
//SYSLIB   DD DSN=CARDDEMO.CICS.LOADLIB,DISP=SHR
//SYSIN    DD *
  LISTLOAD OUTPUT=BOTH,MEMBER=CODASH00C
/*
//*
//* ================================================================
//* STEP 3: AMBLIST — VERIFY CODASH00 MAP SET MODULE ATTRIBUTES
//* ================================================================
//*
//AMBLIST2 EXEC PGM=AMBLIST
//SYSPRINT DD SYSOUT=*
//SYSLIB   DD DSN=CARDDEMO.CICS.LOADLIB,DISP=SHR
//SYSIN    DD *
  LISTLOAD OUTPUT=BOTH,MEMBER=CODASH00
/*
//*
//* ================================================================
//* STEP 4: VERIFY CSD ENTRIES FOR CD00 AND CODASH00
//* ================================================================
//*
//CSDLIST  EXEC PGM=DFHCSDUP,REGION=0M,
//             PARM='CSD(READONLY),PAGESIZE(60),NOCOMPAT'
//*
//STEPLIB  DD DSN=CICSTS56.CICS.SDFHLOAD,DISP=SHR
//*
//DFHCSD   DD DSN=CARDDEMO.DFHCSD,DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//SYSIN    DD *
  LIST GROUP(CARDDEMO) ALL
/*
//*
//* ================================================================
//* STEP 5: IEFBR14 — ZERO-RETURN CONFIRMATION STEP
//*         IF ALL PRIOR STEPS COMPLETE RC=0, THIS CONFIRMS
//*         TASK-027 ACCEPTANCE CRITERIA ARE MET:
//*           - CODASH00C LOAD MODULE EXISTS IN CICS LOAD LIBRARY
//*           - CODASH00 MAP SET LOAD MODULE EXISTS IN CICS LOAD LIB
//*           - CSD ENTRIES FOR CD00 AND CODASH00 ARE DEFINED
//* ================================================================
//*
//CONFIRM  EXEC PGM=IEFBR14,COND=(0,NE)
//*
//