(function () {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('personne', {
                parent: 'entity',
                url: '/personne?page&sort&search',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.personne.home.title',
                    typeP:'PHYSIQUE'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/personne/personnes.html',
                        controller: 'PersonneDialogController',
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
                    search: null
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
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }], $uibModalInstance: function () {
                        return null;
                    }
                }
            })
            .state('personne-morale', {
                parent: 'entity',
                url: '/entreprise?page&sort&search',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.personne.home.title',
                    typeP:'MORALE'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/personne/personnes.html',
                        controller: 'PersonneDialogController',
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
                    search: null
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
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }], $uibModalInstance: function () {
                        return null;
                    }
                }
            })
            .state('operations-personne', {
                parent: 'entity',
                url: '/operations-client?page&sort&search',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.personne.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/operation/operations.html',
                        controller: 'OperationController',
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
                    personneId:null
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
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    validation: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    }
                }
            });
    }

})();
