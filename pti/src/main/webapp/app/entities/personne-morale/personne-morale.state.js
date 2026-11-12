(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('personne-morale', {
            parent: 'entity',
            url: '/personne-morale?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.personneMorale.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/personne-morale/personne-morales.html',
                    controller: 'PersonneMoraleController',
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
                    $translatePartialLoader.addPart('personneMorale');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('personne-morale-detail', {
            parent: 'personne-morale',
            url: '/personne-morale/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.personneMorale.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/personne-morale/personne-morale-detail.html',
                    controller: 'PersonneMoraleDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('personneMorale');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'PersonneMorale', function($stateParams, PersonneMorale) {
                    return PersonneMorale.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'personne-morale',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('personne-morale-detail.edit', {
            parent: 'personne-morale-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-morale/personne-morale-dialog.html',
                    controller: 'PersonneMoraleDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['PersonneMorale', function(PersonneMorale) {
                            return PersonneMorale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('personne-morale.new', {
            parent: 'personne-morale',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-morale/personne-morale-dialog.html',
                    controller: 'PersonneMoraleDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                raisonSociale: null,
                                registreCommerce: null,
                                numreoIfu: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('personne-morale', null, { reload: 'personne-morale' });
                }, function() {
                    $state.go('personne-morale');
                });
            }]
        })
        .state('personne-morale.edit', {
            parent: 'personne-morale',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-morale/personne-morale-dialog.html',
                    controller: 'PersonneMoraleDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['PersonneMorale', function(PersonneMorale) {
                            return PersonneMorale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('personne-morale', null, { reload: 'personne-morale' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('personne-morale.delete', {
            parent: 'personne-morale',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/personne-morale/personne-morale-delete-dialog.html',
                    controller: 'PersonneMoraleDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['PersonneMorale', function(PersonneMorale) {
                            return PersonneMorale.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('personne-morale', null, { reload: 'personne-morale' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
