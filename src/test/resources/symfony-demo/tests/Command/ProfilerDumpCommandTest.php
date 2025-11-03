<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace App\Tests\Command;


final class ProfilerDumpCommandTest extends AbstractCommandTestCase
{
    public function testListUsers(): void
    {
        $tester = $this->executeCommand(
            []
        );
        dump($tester->getDisplay());
    }
    protected function getCommandName(): string
    {
        return 'app:profiler-dump';
    }
}
