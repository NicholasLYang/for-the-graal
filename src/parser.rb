require 'sxp'

def parse_program(program)
  cst = SXP.read program

  quote_strings (desugar_operator cst)
end

def quote_strings(program)
  program.map do |elem|
    if elem.is_a?(Array)
      quote_strings(elem)
    elsif elem.is_a?(String)
      [:quote, elem]
    else
      elem
    end
  end
end

def desugar_operator(program)
  op = program[0]

  if op == :+ || op == :- || op == :/ || op == :*
    # If we have a binary op with more than two args
    # we convert it to a nested form with the same binary
    # op
    if program.length > 3
      program[2] = desugar_operator([program[0], *program[2..-1]])
    end
    program.take(3)
  else
    program.each_with_index do |e, i|
      if e.is_a?(Array)
        program[i] = desugar_operator(e)
      end
    end
    program
  end
end
