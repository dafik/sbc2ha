import { JsonDPipe } from './json-d.pipe';

describe('JsonDPipe', () => {
  it('create an instance', () => {
    const pipe = new JsonDPipe();
    expect(pipe).toBeTruthy();
  });
});
