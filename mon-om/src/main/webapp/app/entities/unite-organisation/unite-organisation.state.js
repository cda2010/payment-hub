(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('unite-organisation', {
            parent: 'entity',
            url: '/unite-organisation',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'UniteOrganisations'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/unite-organisation/unite-organisations.html',
                    controller: 'UniteOrganisationsController',
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
            }
        })
        .state('unite-organisation-detail', {
            parent: 'unite-organisation',
            url: '/unite-organisation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'UniteOrganisation'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-detail.html',
                    controller: 'UniteOrganisationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'UniteOrganisation', function($stateParams, UniteOrganisation) {
                    return UniteOrganisation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'unite-organisation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('unite-organisation-detail.edit', {
            parent: 'unite-organisation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                    controller: 'UniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('unite-organisation.new', {
            parent: 'unite-organisation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                    controller: 'UniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('unite-organisation');
                });
            }]
        })
        .state('unite-organisation.edit', {
            parent: 'unite-organisation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
                    controller: 'UniteOrganisationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('unite-organisation.delete', {
            parent: 'unite-organisation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/unite-organisation/unite-organisation-delete-dialog.html',
                    controller: 'UniteOrganisationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['UniteOrganisation', function(UniteOrganisation) {
                            return UniteOrganisation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('unite-organisation', null, { reload: 'unite-organisation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
