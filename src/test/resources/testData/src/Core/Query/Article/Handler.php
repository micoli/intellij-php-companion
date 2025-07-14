<?php

declare(strict_types=1);

namespace App\Core\Query\Article;

use App\Core\Models\Article;
use App\Core\Query\ArticleDTO;
use App\Infrastructure\Bus\Handler\QueryHandlerInterface;

final readonly class Handler implements QueryHandlerInterface
{
    public function __construct(
    ) {
    }

    public function __invoke(Query $query): Result
    {
        return new Result(new ArticleDTO());
    }
}
