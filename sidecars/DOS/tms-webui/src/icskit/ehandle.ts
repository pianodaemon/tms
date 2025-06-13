export const catchWithExpError = <T, E extends new (...args: any[]) => Error>(
  promise: Promise<T>,
  errorsToCatch?: E[]
): Promise<[undefined, T] | [InstanceType<E>]> =>
  promise
    .then(data => [undefined, data] as [undefined, T])
    .catch(error => {
      if (!errorsToCatch || errorsToCatch.some(Err => error instanceof Err)) {
        return [error] as [InstanceType<E>];
      }
      throw error;
    });

export const catchError = <T>(promise: Promise<T>): Promise<[undefined, T] | [Error]> =>
  promise
    .then(data => [undefined, data] as [undefined, T])
    .catch(error => [error]);

export default { catchWithExpError, catchError }