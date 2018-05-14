import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/SharedModule';
import { MdbRoutingModule, routingComponents } from './MdbRoutingModule';
import { AlgorithmDetail } from './algorithms/AlgorithmDetail';
import { AlgorithmsTable } from './algorithms/AlgorithmsTable';
import { ArgumentEnumDialog } from './commands/ArgumentEnumDialog';
import { CommandDetail } from './commands/CommandDetail';
import { CommandsTable } from './commands/CommandsTable';
import { IssueCommandDialog } from './commands/IssueCommandDialog';
import { ContainerDetail } from './containers/ContainerDetail';
import { ContainersTable } from './containers/ContainersTable';
import { CompareParameterDialog } from './parameters/CompareParameterDialog';
import { ParameterCalibration } from './parameters/ParameterCalibration';
import { ParameterDetail } from './parameters/ParameterDetail';
import { ParameterValuesTable } from './parameters/ParameterValuesTable';
import { ParametersTable } from './parameters/ParametersTable';
import { SelectRangeDialog } from './parameters/SelectRangeDialog';
import { SetParameterDialog } from './parameters/SetParameterDialog';
import { SeverityMeter } from './parameters/SeverityMeter';
import { PolynomialPipe } from './pipes/PolynomialPipe';
import { MdbPageTemplate } from './template/MdbPageTemplate';
import { MdbToolbar } from './template/MdbToolbar';

const pipes = [
  PolynomialPipe,
];

const dialogComponents = [
  ArgumentEnumDialog,
  CompareParameterDialog,
  IssueCommandDialog,
  SelectRangeDialog,
  SetParameterDialog,
];

@NgModule({
  imports: [
    SharedModule,
    MdbRoutingModule,
  ],
  declarations: [
    dialogComponents,
    pipes,
    routingComponents,
    AlgorithmsTable,
    AlgorithmDetail,
    CommandsTable,
    CommandDetail,
    ContainersTable,
    ContainerDetail,
    MdbPageTemplate,
    MdbToolbar,
    ParametersTable,
    ParameterCalibration,
    ParameterDetail,
    ParameterValuesTable,
    SeverityMeter,
  ],
  entryComponents: [
    dialogComponents,
  ],
})
export class MdbModule {
}