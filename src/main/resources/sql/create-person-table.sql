DROP TABLE IF EXISTS public.person;

CREATE TABLE public.person
(
    id   int  NOT NULL PRIMARY KEY,
    name text NOT NULL
);

INSERT INTO public.person
VALUES (1, 'Bob');

INSERT INTO public.person
VALUES (2, 'Alice');
