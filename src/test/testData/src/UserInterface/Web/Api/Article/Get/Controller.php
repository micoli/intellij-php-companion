<?php

declare(strict_types=1);

namespace App\UserInterface\Web\Api\Article\Get;

use App\Core\Article\Application\Query\Article as ArticleQuery;
use App\Core\Id\ArticleId;
use App\Core\Member\Domain\Entity\User;
use App\Infrastructure\Bus\QueryBusInterface;
use App\UserInterface\Web\Api\Article\ArticleEnricher;
use Nelmio\ApiDocBundle\Annotation\Model;
use OpenApi\Attributes as OA;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpKernel\Attribute\AsController;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\CurrentUser;

#[AsController]
final readonly class Controller
{
    public function __construct(
        private QueryBusInterface $queryBus,
        private ArticleEnricher $articleEnricher,
    ) {
    }

    #[Route(
        '/api/article/{articleId}',
        name: 'api_article_get',
        methods: ['GET'],
    )]
    public function article(
        #[CurrentUser] ?User $user,
        ArticleId $articleId,
    ): JsonResponse {
        return new JsonResponse(null);
    }
}
