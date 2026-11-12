(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('client', {
            parent: 'entity',
            url: '/client?page&sort&search',
            data: {
                authorities: ['ROLE_CONSULT_CLIENT'],
                pageTitle: 'ptiApp.client.home.title',
                typeP:'PHYSIQUE'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/client/clients.html',
                    controller: 'ClientController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null,
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('client');
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
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        }).state('entreprises', {
            parent: 'entity',
            url: '/entreprises?page&sort&search',
            data: {
                authorities: ['ROLE_CONSULT_CLIENT'],
                pageTitle: 'ptiApp.client.home.title',
                typeP:'MORALE'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/client/clients.html',
                    controller: 'ClientController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null,
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('client');
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
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        }).state('client-souscription', {
            parent: 'entity',
            url: '/client-souscription',
            data: {
                authorities: ['ROLE_DO_SOUSCRIPTION_CLIENT'],
                pageTitle: 'ptiApp.client.home.title',
                typeP:'PHYSIQUE'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/client/client-dialog.html',
                    controller: 'ClientDialogController',
                    controllerAs: 'vm'
                }
            },
            params: {
                datas:null,
                isViewing:false,
                back:null,
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('client');
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
                    $translatePartialLoader.addPart('document');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        }).state('entreprise-souscription', {
            parent: 'entity',
            url: '/entreprise-souscription',
            data: {
                authorities: ['ROLE_DO_SOUSCRIPTION_CLIENT'],
                pageTitle: 'ptiApp.client.home.title',
                typeP:'MORALE'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/client/client-dialog.html',
                    controller: 'ClientDialogController',
                    controllerAs: 'vm'
                }
            },
            params: {
                datas:null,
                isViewing:false
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('client');
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
                    $translatePartialLoader.addPart('document');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        });
    }

})();
