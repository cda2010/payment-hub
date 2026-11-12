(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('template-message', {
            parent: 'entity',
            url: '/template-message?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.templateMessage.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/template-message/template-messages.html',
                    controller: 'TemplateMessageController',
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
                    $translatePartialLoader.addPart('templateMessage');
                    $translatePartialLoader.addPart('langues');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('template-message-detail', {
            parent: 'template-message',
            url: '/template-message/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.templateMessage.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/template-message/template-message-detail.html',
                    controller: 'TemplateMessageDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('templateMessage');
                    $translatePartialLoader.addPart('langues');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'TemplateMessage', function($stateParams, TemplateMessage) {
                    return TemplateMessage.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'template-message',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('template-message-detail.edit', {
            parent: 'template-message-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/template-message/template-message-dialog.html',
                    controller: 'TemplateMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['TemplateMessage', function(TemplateMessage) {
                            return TemplateMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('template-message.new', {
            parent: 'template-message',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/template-message/template-message-dialog.html',
                    controller: 'TemplateMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                etiquette: null,
                                contenu: null,
                                langue: null,
                                destination:null,
                                id: null,
                                fseId:null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('template-message', null, { reload: 'template-message' });
                }, function() {
                    $state.go('template-message');
                });
            }]
        })
        .state('template-message.edit', {
            parent: 'template-message',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/template-message/template-message-dialog.html',
                    controller: 'TemplateMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['TemplateMessage', function(TemplateMessage) {
                            return TemplateMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('template-message', null, { reload: 'template-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('template-message.delete', {
            parent: 'template-message',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/template-message/template-message-delete-dialog.html',
                    controller: 'TemplateMessageDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['TemplateMessage', function(TemplateMessage) {
                            return TemplateMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('template-message', null, { reload: 'template-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
