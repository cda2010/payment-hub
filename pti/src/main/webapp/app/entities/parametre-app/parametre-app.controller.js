(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParametreAppController', ParametreAppController);

    ParametreAppController.$inject = ['ParametreApp','InitCrud','$scope'];

    function ParametreAppController(ParametreApp,InitCrud,$scope) {

        var vm = this;
        vm.title = "Liste des paramètres";
        vm.columns = [{
            name: 'nom',
            title: 'Nom'
        }, {
            name: 'type',
            title: 'Type'
        }, {
            name: 'valeur',
            title: 'Valeur'
        },{
            name: 'codeFse',
            title: 'Fournisseur Service'
        }];

        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                nom: null,
                valeur: null,
                type:null,
                id: null
            },
            entity:ParametreApp ,
            addTemplateUrl: "parametre-app/parametre-app-dialog.html",
            deleteTemplateUrl: "parametre-app/parametre-app-delete-dialog.html",
            controller: "ParametreApp",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
        console.log('datas',datas);

    }
})();
