(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BrancheActiviteController', BrancheActiviteController);

    BrancheActiviteController.$inject = ['$scope','InitCrud','$state', 'BrancheActivite', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function BrancheActiviteController($scope,InitCrud,$state, BrancheActivite, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title = "Liste des branches d'activités";
        vm.columns = [{
            name: 'code',
            title: 'Code'
        }, {
            name: 'libelle',
            title: 'Libelle'
        }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                libelle: null,
                id: null
            },
            entity: BrancheActivite,
            addTemplateUrl: "branche-activite/branche-activite-dialog.html",
            deleteTemplateUrl: "branche-activite/branche-activite-delete-dialog.html",
            controller: "BrancheActivite",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();