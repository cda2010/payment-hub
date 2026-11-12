(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeLocaliteController', TypeLocaliteController);

    TypeLocaliteController.$inject = ['$state', 'TypeLocalite', '$scope', 'InitCrud'];

    function TypeLocaliteController($state, TypeLocalite, $scope, InitCrud) {

        var vm = this;

        vm.columns = [{
            name: 'libelle',
            title: 'Nom'
        }, {
            name: 'libellePays',
            title: 'Pays'
        },{
            name: 'niveau',
            title: 'Niveau'
        }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true
        };

        var datas = {
            vm: vm,
            entityDatas: {
                libelle: null,
                paysId:null,
                niveau: null,
                id: null
            },
            entity: TypeLocalite,
            addTemplateUrl: "type-localite/type-localite-dialog.html",
            deleteTemplateUrl: "type-localite/type-localite-delete-dialog.html",
            controller: "TypeLocalite",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
