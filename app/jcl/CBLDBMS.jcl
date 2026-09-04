//CBLDBMS  JOB (ACCT),'CARDDEMO BUILD',
//             CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),
//             NOTIFY=&SYSUID
//*
//*=================================================================*
//* JOB  : CBLDBMS                                                  *
//* DESC : CARDDEMO - BMS MAP COMPILATION AND LINK-EDIT             *
//*        Compiles all BMS map source members and link-edits        *
//*        the resulting object decks into the CICS load library.   *
//*                                                                  *
//* TRANSACTION ID : 24137                                           *
//*                                                                  *
//* STEPS:                                                           *
//*   BMSCOMP  - Assemble BMS map source using DFHMAPS              *
//*   BMSLKED  - Link-edit assembled map object into load library    *
//*                                                                  *
//* CHANGE HISTORY:                                                  *
//*   YYYY-MM-DD  Initial creation                                   *
//*   YYYY-MM-DD  Added CODASH00.bms for dashboard map set (TASK-012)*
//*=================================================================*
//*
//JOBLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//*
//*-----------------------------------------------------------------*
//* STEP 1: ASSEMBLE EXISTING BMS MAPS                              *
//*-----------------------------------------------------------------*
//*
//* --- COADM1A (Admin Menu Map) ---
//COADM1A  EXEC PGM=ASMA90,
//             PARM='OBJECT,NODECK,NOLIST'
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&COADM1AO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(5,5))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.BMS(COADM1A),DISP=SHR
//*
//* --- COMEN1A (Customer Menu Map) ---
//COMEN1A  EXEC PGM=ASMA90,
//             PARM='OBJECT,NODECK,NOLIST'
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&COMEN1AO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(5,5))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.BMS(COMEN1A),DISP=SHR
//*
//* --- CACTVWA (Account View Map) ---
//CACTVWA  EXEC PGM=ASMA90,
//             PARM='OBJECT,NODECK,NOLIST'
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&CACTVWAO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(5,5))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.BMS(CACTVWA),DISP=SHR
//*
//*-----------------------------------------------------------------*
//* STEP 2: ASSEMBLE CODASH00 BMS MAP (NEW - TASK-012)              *
//*         Dashboard consolidated account tiles map set.           *
//*         This step MUST precede COBOL compilation of CODASH00C   *
//*         because it generates the CODASH00.CPY symbolic map      *
//*         copybook required by CODASH00C.cbl.                     *
//*-----------------------------------------------------------------*
//CODASH00 EXEC PGM=ASMA90,
//             PARM='OBJECT,NODECK,NOLIST'
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&CODASH0O,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(5,5))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.BMS(CODASH00),DISP=SHR
//*
//*-----------------------------------------------------------------*
//* STEP 3: GENERATE CODASH00.CPY SYMBOLIC MAP COPYBOOK (NEW)       *
//*         Re-assemble CODASH00.bms with MAP option to produce     *
//*         the symbolic map copybook for use by CODASH00C.cbl.     *
//*         Output is written to the BMS copybook library.          *
//*-----------------------------------------------------------------*
//CDASHCPY EXEC PGM=ASMA90,
//             PARM='OBJECT,NODECK,NOLIST,MAP'
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS(CODASH00),
//             DISP=SHR
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.BMS(CODASH00),DISP=SHR
//*
//*-----------------------------------------------------------------*
//* STEP 4: LINK-EDIT EXISTING BMS MAP LOAD MODULES                 *
//*-----------------------------------------------------------------*
//*
//* --- Link COADM1A ---
//LKCOADM  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS',
//             COND=(0,NE,COADM1A)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COADM1A),
//             DISP=SHR
//SYSLIN   DD DSN=&&COADM1AO,DISP=(OLD,DELETE)
//*
//* --- Link COMEN1A ---
//LKCOMEN  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS',
//             COND=(0,NE,COMEN1A)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COMEN1A),
//             DISP=SHR
//SYSLIN   DD DSN=&&COMEN1AO,DISP=(OLD,DELETE)
//*
//* --- Link CACTVWA ---
//LKCACTVW EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS',
//             COND=(0,NE,CACTVWA)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(CACTVWA),
//             DISP=SHR
//SYSLIN   DD DSN=&&CACTVWAO,DISP=(OLD,DELETE)
//*
//*-----------------------------------------------------------------*
//* STEP 5: LINK-EDIT CODASH00 MAP SET LOAD MODULE (NEW - TASK-012) *
//*         Produces CODASH00 load module in CICS load library.     *
//*         COND ensures this step only runs if assembly succeeded. *
//*-----------------------------------------------------------------*
//LKCDASH  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS',
//             COND=(0,NE,CODASH00)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(CODASH00),
//             DISP=SHR
//SYSLIN   DD DSN=&&CODASH0O,DISP=(OLD,DELETE)
//*
//