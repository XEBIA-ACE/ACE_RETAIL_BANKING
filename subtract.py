"""Simple subtraction utility."""


def subtract(a, b):
    """Return the difference of a and b (a - b = c).

    >>> subtract(5, 3)
    2
    >>> subtract(2.5, 0.5)
    2.0
    """
    return a - b


if __name__ == "__main__":
    import doctest

    doctest.testmod()
