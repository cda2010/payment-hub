(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('role-validation', {
            parent: 'entity',
            url: '/role-validation?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.roleValidation.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/role-validation/role-validations.html',
                    controller: 'RoleValidationController',
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
                    $translatePartialLoader.addPart('roleValidation');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('role-validation-detail', {
            parent: 'role-validation',
            url: '/role-validation/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.roleValidation.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/role-validation/role-validation-detail.html',
                    controller: 'RoleValidationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('roleValidation');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'RoleValidation', function($stateParams, RoleValidation) {
                    return RoleValidation.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'role-validation',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('role-validation-detail.edit', {
            parent: 'role-validation-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                    controller: 'RoleValidationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['RoleValidation', function(RoleValidation) {
                            return RoleValidation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('role-validation.new', {
            parent: 'role-validation',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                    controller: 'RoleValidationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                typeService: null,
                                operateur: null,
                                montant: null,
                                role: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('role-validation', null, { reload: 'role-validation' });
                }, function() {
                    $state.go('role-validation');
                });
            }]
        })
        .state('role-validation.edit', {
            parent: 'role-validation',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                    controller: 'RoleValidationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['RoleValidation', function(RoleValidation) {
                            return RoleValidation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('role-validation', null, { reload: 'role-validation' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('role-validation.delete', {
            parent: 'role-validation',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/role-validation/role-validation-delete-dialog.html',
                    controller: 'RoleValidationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['RoleValidation', function(RoleValidation) {
                            return RoleValidation.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('role-validation', null, { reload: 'role-validation' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
