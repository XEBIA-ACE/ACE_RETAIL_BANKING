//CICCMP   JOB (ACCT),'CARDDEMO BUILD',
//             CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),
//             NOTIFY=&SYSUID
//*
//*=================================================================*
//* JOB  : CICCMP                                                   *
//* DESC : CARDDEMO - CICS COBOL COMPILATION AND LINK-EDIT          *
//*        Translates, compiles, and link-edits all CICS COBOL      *
//*        programs into the CICS load library.                     *
//*                                                                  *
//* TRANSACTION ID : 24136                                           *
//*                                                                  *
//* STEPS (per program):                                             *
//*   xxxxTRAN - CICS translator (DFHECP1$)                         *
//*   xxxxCOMP - COBOL compiler  (IGYCRCTL)                         *
//*   xxxxLKED - Link-editor     (IEWL)                             *
//*                                                                  *
//* PREREQUISITE: CBLDBMS job (transaction 24137) must complete     *
//*   successfully before this job is submitted, so that the        *
//*   CODASH00.CPY symbolic map copybook is available in            *
//*   AWS.M2.CARDDEMO.SOURCE.CPYBMS for CODASH00C compilation.      *
//*                                                                  *
//* CHANGE HISTORY:                                                  *
//*   YYYY-MM-DD  Initial creation                                   *
//*   YYYY-MM-DD  Added CODASH00C.cbl for dashboard controller      *
//*               (TASK-012). CBLDBMS must precede this job.        *
//*=================================================================*
//*
//JOBLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//*
//*-----------------------------------------------------------------*
//* COMMON PROCEDURE LIBRARY                                        *
//*-----------------------------------------------------------------*
//PROCLIB  JCLLIB ORDER=(SYS1.PROCLIB)
//*
//*=================================================================*
//* COSGN00C - SIGN-ON CONTROLLER                                   *
//*=================================================================*
//*
//SGNTRAN  EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&SGNTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(COSGN00C),DISP=SHR
//*
//SGNCOMP  EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,SGNTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&SGNCOMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&SGNTRNO,DISP=(OLD,DELETE)
//*
//SGNLKED  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,SGNCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COSGN00C),
//             DISP=SHR
//SYSLIN   DD DSN=&&SGNCOMPO,DISP=(OLD,DELETE)
//*
//*=================================================================*
//* COADM01C - ADMIN MENU CONTROLLER                                *
//*=================================================================*
//*
//ADMTRAN  EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&ADMTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(COADM01C),DISP=SHR
//*
//ADMCOMP  EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,ADMTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&ADMCOMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&ADMTRNO,DISP=(OLD,DELETE)
//*
//ADMLKED  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,ADMCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COADM01C),
//             DISP=SHR
//SYSLIN   DD DSN=&&ADMCOMPO,DISP=(OLD,DELETE)
//*
//*=================================================================*
//* COMEN01C - CUSTOMER MENU CONTROLLER                             *
//*=================================================================*
//*
//MENTRAN  EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&MENTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(COMEN01C),DISP=SHR
//*
//MENCOMP  EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,MENTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&MENCOMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&MENTRNO,DISP=(OLD,DELETE)
//*
//MENLKED  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,MENCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COMEN01C),
//             DISP=SHR
//SYSLIN   DD DSN=&&MENCOMPO,DISP=(OLD,DELETE)
//*
//*=================================================================*
//* COACTVWC - ACCOUNT VIEW CONTROLLER                              *
//*=================================================================*
//*
//AVWTRAN  EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&AVWTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(COACTVWC),DISP=SHR
//*
//AVWCOMP  EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,AVWTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&AVWCOMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&AVWTRNO,DISP=(OLD,DELETE)
//*
//AVWLKED  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,AVWCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COACTVWC),
//             DISP=SHR
//SYSLIN   DD DSN=&&AVWCOMPO,DISP=(OLD,DELETE)
//*
//*=================================================================*
//* COACTUPC - ACCOUNT UPDATE CONTROLLER                            *
//*=================================================================*
//*
//AUPTRAN  EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&AUPTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(COACTUPC),DISP=SHR
//*
//AUPCOMP  EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,AUPTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&AUPCOMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&AUPTRNO,DISP=(OLD,DELETE)
//*
//AUPLKED  EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,AUPCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(COACTUPC),
//             DISP=SHR
//SYSLIN   DD DSN=&&AUPCOMPO,DISP=(OLD,DELETE)
//*
//*=================================================================*
//* CODASH00C - DASHBOARD CONTROLLER (NEW - TASK-012)               *
//*                                                                  *
//* PREREQUISITE: CBLDBMS job must have completed successfully so   *
//*   that CODASH00.CPY is present in CPYBMS library before this    *
//*   set of steps executes. The CBLDBMS job (transaction 24137)    *
//*   assembles CODASH00.bms and writes the symbolic map copybook   *
//*   to AWS.M2.CARDDEMO.SOURCE.CPYBMS(CODASH00).                  *
//*                                                                  *
//* Steps:                                                           *
//*   CDSHTRAN - CICS translator                                    *
//*   CDSHCOMP - COBOL compiler (requires CODASH00.CPY in CPYBMS)  *
//*   CDSHLKED - Link-editor -> CODASH00C load module               *
//*=================================================================*
//*
//CDSHTRAN EXEC PGM=DFHECP1$,
//             PARM='CICS,COBOL3,NOEDF'
//STEPLIB  DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&CDSHTRNO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=AWS.M2.CARDDEMO.SOURCE.CBL(CODASH00C),DISP=SHR
//*
//CDSHCOMP EXEC PGM=IGYCRCTL,
//             PARM='LIB,APOST,NOSEQ,NOLIST,NOMAP,OPT(FULL)',
//             COND=(0,NE,CDSHTRAN)
//STEPLIB  DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSLIB   DD DSN=AWS.M2.CARDDEMO.SOURCE.CPY,DISP=SHR
//         DD DSN=AWS.M2.CARDDEMO.SOURCE.CPYBMS,DISP=SHR
//         DD DSN=CICSTS.V6R1M0.CICS.SDFHCOB,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSPRINT DD SYSOUT=*
//SYSPUNCH DD DSN=&&CDSHCMPO,DISP=(NEW,PASS),
//             UNIT=SYSDA,SPACE=(TRK,(10,10))
//SYSIN    DD DSN=&&CDSHTRNO,DISP=(OLD,DELETE)
//*
//CDSHLKED EXEC PGM=IEWL,
//             PARM='LIST,XREF,LET,RENT,REUS,AMODE(31),RMODE(ANY)',
//             COND=(0,NE,CDSHCOMP)
//SYSLIB   DD DSN=CICSTS.V6R1M0.CICS.SDFHLOAD,DISP=SHR
//         DD DSN=IGY.V6R3M0.SIGYCOMP,DISP=SHR
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(2,1))
//SYSPRINT DD SYSOUT=*
//SYSLMOD  DD DSN=AWS.M2.CARDDEMO.CICS.LOADLIB(CODASH00C),
//             DISP=SHR
//SYSLIN   DD DSN=&&CDSHCMPO,DISP=(OLD,DELETE)
//*
//