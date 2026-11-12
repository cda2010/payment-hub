(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalController', CanalController);

    CanalController.$inject = ['InitCrud', '$scope', 'Canal'];

    function CanalController(InitCrud, $scope, Canal) {
        var vm = this;
        vm.title = "Liste des canaux";
        vm.columns = [{
            name: 'codeCanal',
            title: 'Code'
        }, {
            name: 'libelleCanal',
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
                codeCanal: null,
                libelleCanal: null,
                id: null
            },
            entity: Canal,
            addTemplateUrl: "canal/canal-dialog.html",
            deleteTemplateUrl: "canal/canal-delete-dialog.html",
            controller: "Canal",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();