import { TestBed } from '@angular/core/testing';

import { RemoteLogService } from './remote-log.service';

describe('RemoteLogServiceService', () => {
  let service: RemoteLogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RemoteLogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
