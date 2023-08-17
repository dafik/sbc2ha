import { TestBed } from '@angular/core/testing';

import { AppConfigFromYamlService } from './app-config-from-yaml.service';

describe('AppConfigFromJsonService', () => {
  let service: AppConfigFromYamlService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AppConfigFromYamlService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
