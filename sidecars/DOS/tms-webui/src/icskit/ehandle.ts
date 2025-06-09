function catchWithExpError<T, E extends new (...args: any[]) => Error>(
  promise: Promise<T>,
  errorsToCatch?: E[]
): Promise<[undefined, T] | [InstanceType<E>]> {

  return promise.then((data) => [undefined, data] as [undefined, T])
    .catch((error) => {
      if (!errorsToCatch || errorsToCatch.some((ErrType) => error instanceof ErrType)) return [error];

      throw error; // Re-throw unexpected errors
    });
}