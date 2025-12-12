
:- dynamic enfermedad/5.
:- dynamic diagnostico_paciente/3.

% Base de conocimientos de enfermedades.


% coincide_sintomas(+ListaSintomas, -Enfermedad).
coincide_sintomas(ListaSintomas, Enfermedad) :-
	enfermedad(_, Enfermedad, Sintomas, _, _),
	incluidos(ListaSintomas, Sintomas).

% contiene(+Elem, +Lista)
contiene(Elem, [Elem|_]).
contiene(Elem, [_|T]) :-
	contiene(Elem, T).

% incluidos(+Sublista, +Lista)
incluidos([], _).
incluidos([H|T], Lista) :-
	contiene(H, Lista),
	incluidos(T, Lista).

% enfermedades_cronicas(-Enfermedad).
enfermedades_cronicas(Enfermedad) :-
	enfermedad(_, Enfermedad, _, cronica, _).

% diagnostico(+SintomasPaciente, -Id, -Enfermedad, -SintomasEnfermedad, -Categoria, -Recomendacion)
diagnostico(SintomasPaciente, Id, Enfermedad, SintomasEnfermedad, Categoria, Recomendacion) :-
	enfermedad(Id, Enfermedad, SintomasEnfermedad, Categoria, Recomendacion),
	member(S, SintomasPaciente),
	contiene(S, SintomasEnfermedad).

% diagnostico_categoria(+Categoria, -Id, -Enfermedad)
diagnostico_categoria(Categoria, Id, Enfermedad) :-
	enfermedad(Id, Enfermedad, _, Categoria, _).

% recomendacion(+Enfermedad, -Recomendacion)
recomendacion(Enfermedad, Recomendacion) :-
	enfermedad(_, Enfermedad, _, _, Recomendacion).

% enfermedades_por_sintoma(+Sintoma, -Enfermedad)
enfermedades_por_sintoma(Sintoma, Enfermedad) :-
	enfermedad(_, Enfermedad, Sintomas, _, _),
	contiene(Sintoma, Sintomas).

% consulta_diagnostico(+Nombre, +Edad, -Id)
consulta_diagnostico(Nombre, Edad, Id) :-
	diagnostico_paciente(Id, Nombre, Edad).
