library bmi version '1.0'

using FHIR version '3.0.0'

include FHIRHelpers version '3.0.0' called FHIRHelpers

valueset "Pregnant" : '2.16.840.1.113883.3.600.1.1623'
valueset "PalliativeCare" : '2.16.840.1.113883.3.600.1.1579'
valueset "BmiObservation" : 'BmiObservationValueSet'
valueset "HeigthObservation" : 'HeigthObservationValueSet'
valueset "WeigthObservation" : 'WeigthObservationValueSet'

context Patient


define BMIObservations:
	[FHIR.Observation] O
	sort by issued.value

/////////////////////////////////////////////////////////////////////

define PatientElder18Younger65:
   ( AgeInYears() >= 18 and AgeInYears() <= 65 )

/////////////////////////////////////////////////////////////////////

define PallativeCareEntries:
   [Observation : PalliativeCare] O
	where O.issued after Now() - 1 year

define NotReceivingPallativeCare:
	not Exists( PallativeCareEntries )

/////////////////////////////////////////////////////////////////////

define PregnantEntries:
   [Observation : Pregnant] O
	where O.issued after Now() - 1 year

define NotPregnant:
	not Exists( PallativeCareEntries )

/////////////////////////////////////////////////////////////////////

define HeigthObservations:
   [Observation : HeigthObservation] O
	where O.issued after Now() - 1 year
	sort by issued.value

define HeigthMeasured:
	Exists( HeigthObservations )

define function NormalizeLength( quantity FHIR.Quantity ) :
  case quantity.unit.value
     when 'cm' then quantity.value.value/100
     when 'mm' then quantity.value.value/1000
     when 'km' then quantity.value.value*1000
     else quantity.value.value
  end

define MostRecentHeigth:
	Last( HeigthObservations ).value.value

/////////////////////////////////////////////////////////////////////

define BmiObservations:
   [Observation : BmiObservation] O
	where O.issued after Now() - 1 year
	sort by issued.value

define BmiMeasured:
	Exists( BmiObservations )

define MostRecentBmi:
	Last( BmiObservations ).value.value

/////////////////////////////////////////////////////////////////////

define WeigthObservations:
   [Observation : WeigthObservation] O
	where O.issued after Now() - 1 year
	sort by issued.value

define WeigthMeasured:
	Exists( WeigthObservations )

define MostRecentWeigth:
	Last( WeigthObservations ).value.value

/////////////////////////////////////////////////////////////////////

define function calculateBMI( weight Decimal, heigth Decimal ):
  weight / ( heigth*heigth)

define BMIValue:
	calculateBMI( MostRecentWeigth, MostRecentHeigth )

define BMIQuantity: FHIR.Quantity{
   system:  FHIR.uri{ value: 'http://unitsofmeasure.org' },
   unit:    FHIR.string{ value: 'kg/m2' },
   code :   FHIR.code{ value: 'kg/m2' },
   value:   FHIR.decimal{ value: BMIValue }
}
