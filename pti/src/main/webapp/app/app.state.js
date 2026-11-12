(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('app', {
            abstract: true,
            views: {
                'root@': {
                    templateUrl: 'app/layouts/navbar/partial.html',

                }
            },
            resolve: {
                authorize: ['Auth',
                    function (Auth) {
                        return Auth.authorize();
                    }
                ],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('global');
                    $translatePartialLoader.addPart('operation');
                    $translatePartialLoader.addPart('statutOperation');
                    $translatePartialLoader.addPart('personnePhysique');
                    $translatePartialLoader.addPart('personneMorale');
                    $translatePartialLoader.addPart('sexe');
                    $translatePartialLoader.addPart('piece');
                    $translatePartialLoader.addPart('personne');
                    $translatePartialLoader.addPart('typePersonne');
                    $translatePartialLoader.addPart('compte');
                    $translatePartialLoader.addPart('compteBanque');
                    $translatePartialLoader.addPart('compteAutre');
                    $translatePartialLoader.addPart('client');
                }]
            }
        });
    }
})();
