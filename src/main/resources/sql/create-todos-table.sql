DROP TABLE IF EXISTS public.todos;

CREATE TABLE public.todos
(
    id        int  NOT NULL PRIMARY KEY,
    person_id int  NOT NULL,
    title     text NOT NULL
);

INSERT INTO public.todos
VALUES (1, 1, 'Todo 1');

INSERT INTO public.todos
VALUES (2, 1, 'Todo 2');

INSERT INTO public.todos
VALUES (3, 2, 'Todo 3');

INSERT INTO public.todos
VALUES (4, 2, 'Todo 4');
