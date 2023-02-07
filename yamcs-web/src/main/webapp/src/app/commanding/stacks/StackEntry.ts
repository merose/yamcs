import { Command, Value } from '../../client';
import { CommandHistoryRecord } from '../command-history/CommandHistoryRecord';

export interface StackEntry {
  name: string;
  args: { [key: string]: any; };
  comment?: string;
  extra?: { [key: string]: Value; };
  advanceOn?: AdvanceOnParams;

  command?: Command;

  executionNumber?: number;
  id?: string;
  record?: CommandHistoryRecord;
  err?: string;
}

export interface AdvanceOnParams {
  ack?: string;
  delay?: number;
}
