<?php

declare(strict_types=1);

namespace App\Core\Query\Feed;

use App\Core\Article\Domain\Entity\Feed;
use App\Infrastructure\Bus\Handler\QueryHandlerInterface;

final readonly class Handler implements QueryHandlerInterface
{
    public function __construct(
    ) {
    }

    public function __invoke(Query $query): Result
    {
        return new Result([]),
        );
    }
}
