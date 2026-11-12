(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeDocumentController', TypeDocumentController);

    TypeDocumentController.$inject = ['InitCrud', '$scope', 'TypeDocument'];

    function TypeDocumentController(InitCrud, $scope, TypeDocument) {
        var vm = this;
        vm.title = "Liste des types de document";
        vm.columns = [
            // {
            // name: 'code',
            // title: 'Code'
            // },
            {
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
            entity: TypeDocument,
            addTemplateUrl: "type-document/type-document-dialog.html",
            deleteTemplateUrl: "type-document/type-document-delete-dialog.html",
            controller: "TypeDocument",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
